package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Scanner;

/**
 * GraphQL table-view operations from the JVM (Synergy blocks fetch/XHR from injected scripts).
 * Uses Authorization headers captured from the app's own GraphQL traffic.
 */
public final class TableViewGraphqlClient {

    private static final java.util.concurrent.ConcurrentHashMap<String, String> VIEW_ID_BY_NAME =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<String, JSONObject> FULL_VIEW_BY_NAME =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static volatile long lastAuthWarmUpMs;
    private static final long AUTH_WARMUP_TTL_MS = 5L * 60L * 1000L;

    private static final String GQL_URL = "https://fc-api.paramountmsc.com/graphql/";
    private static final String TABLE_VIEWS_QUERY =
            "query TableViews($user: String!, $tableType: String!) { tableViews(user: $user, tableType: $tableType) { id tableType name "
                    + "columns { orderTable { label selected __typename } packageTable { label selected __typename } "
                    + "lineItemTable { label selected __typename } __typename } teamView default access createdBy updatedBy updatedAt __typename } }";
    private static final String UPDATE_TABLE_VIEW =
            "mutation UpdateTableView($item: TableViewInput!) { updateTableView(item: $item) { id name } }";
    private static final String SAVE_TABLE_VIEW =
            "mutation SaveTableView($item: TableViewInput!) { saveTableView(item: $item) { id name } }";
    private static final String DELETE_TABLE_VIEW =
            "mutation DeleteTableView($id: String!) { deleteTableView(id: $id) { id name } }";

    private static final String CAPTURE_AUTH_JS =
            "(function(){"
                    + "if(window.__ffAuthHookInstalled)return;"
                    + "window.__ffAuthHookInstalled=true;"
                    + "window.__ffAuthHeader=null;"
                    + "window.__ffGqlUrl=null;"
                    + "var xhrOpen=XMLHttpRequest.prototype.open;"
                    + "var xhrSend=XMLHttpRequest.prototype.send;"
                    + "var xhrSetHeader=XMLHttpRequest.prototype.setRequestHeader;"
                    + "XMLHttpRequest.prototype.setRequestHeader=function(h,v){"
                    + "  if(String(h||'').toLowerCase()==='authorization'&&v){window.__ffAuthHeader=String(v);}"
                    + "  return xhrSetHeader.apply(this,arguments);"
                    + "};"
                    + "XMLHttpRequest.prototype.open=function(m,u){this.__ffUrl=u;return xhrOpen.apply(this,arguments);};"
                    + "XMLHttpRequest.prototype.send=function(b){"
                    + "  if(this.__ffUrl&&this.__ffUrl.indexOf('graphql')>=0){window.__ffGqlUrl=this.__ffUrl.split('?')[0];}"
                    + "  return xhrSend.apply(this,arguments);"
                    + "};"
                    + "if(window.__ffOrigFetch||window.fetch){"
                    + "  var f=window.__ffOrigFetch||window.fetch;"
                    + "  window.fetch=function(input,init){"
                    + "    var url=typeof input==='string'?input:(input&&input.url?input.url:'');"
                    + "    if(url.indexOf('graphql')>=0){"
                    + "      window.__ffGqlUrl=url.split('?')[0];"
                    + "      if(init&&init.headers){"
                    + "        var h=init.headers;"
                    + "        if(h.Authorization)window.__ffAuthHeader=h.Authorization;"
                    + "        else if(h.authorization)window.__ffAuthHeader=h.authorization;"
                    + "        else if(typeof h.get==='function'){var a=h.get('Authorization')||h.get('authorization');if(a)window.__ffAuthHeader=a;}"
                    + "      }"
                    + "    }"
                    + "    return f.apply(this,arguments);"
                    + "  };"
                    + "}"
                    + "})();";

    private static final String RESOLVE_AUTH_JS =
            "function walk(o){"
                    + "if(!o||typeof o!=='object')return null;"
                    + "if(typeof o.accessToken==='string')return o.accessToken;"
                    + "if(o.accessToken&&o.accessToken.accessToken)return o.accessToken.accessToken;"
                    + "if(typeof o.token==='string')return o.token;"
                    + "if(typeof o.idToken==='string')return o.idToken;"
                    + "for(var k in o){if(Object.prototype.hasOwnProperty.call(o,k)){var v=walk(o[k]);if(v)return v;}}"
                    + "return null;"
                    + "}"
                    + "function resolveAuth(){"
                    + "if(window.__ffAuthHeader)return window.__ffAuthHeader;"
                    + "try{"
                    + "  var stores=[localStorage,sessionStorage];"
                    + "  for(var s=0;s<stores.length;s++){"
                    + "    var store=stores[s];"
                    + "    for(var i=0;i<store.length;i++){"
                    + "      var v=store.getItem(store.key(i));"
                    + "      if(!v)continue;"
                    + "      try{var t=walk(JSON.parse(v));if(t)return t.indexOf('Bearer')===0?t:'Bearer '+t;}catch(e){}"
                    + "      if(/^ey[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+/.test(v))return 'Bearer '+v;"
                    + "    }"
                    + "  }"
                    + "}catch(e){}"
                    + "return null;"
                    + "}"
                    + "return resolveAuth();";

    private TableViewGraphqlClient() {
    }

    public static void installAuthCaptureHook() {
        runScript(CAPTURE_AUTH_JS);
        FulfillmentJsUtil.installNetworkCaptureHook();
    }

    /** Trigger GraphQL traffic so Authorization header is captured before API lookups. */
    public static void warmUpGraphqlAuth() {
        try {
            installAuthCaptureHook();
            refreshAuthFromUi();
            lastAuthWarmUpMs = System.currentTimeMillis();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.logConsoleMessage("GraphQL auth warm-up interrupted: " + e.getMessage());
        }
    }

    /** Avoid repeated auth/UI refresh when many sort/search tests run back-to-back. */
    public static void warmUpGraphqlAuthIfNeeded() {
        if (lastAuthWarmUpMs > 0
                && System.currentTimeMillis() - lastAuthWarmUpMs < AUTH_WARMUP_TTL_MS) {
            return;
        }
        warmUpGraphqlAuth();
    }

    /**
     * One TableViews query to cache all saved views matching a name prefix (e.g. FF-SORT-).
     * Avoids per-column GraphQL lookups during long sort suites.
     */
    @SuppressWarnings("unchecked")
    public static int prefetchTableViewsByNamePrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return 0;
        }
        try {
            warmUpGraphqlAuthIfNeeded();
            JSONObject vars = buildTableViewsVariables();
            JSONObject res = gql("TableViews", resolveTableViewsQuery(), vars);
            JSONObject data = (JSONObject) res.get("data");
            if (data == null) {
                return 0;
            }
            JSONArray views = (JSONArray) data.get("tableViews");
            if (views == null || views.isEmpty()) {
                return 0;
            }
            String normalizedPrefix = prefix.trim().toLowerCase(Locale.US);
            int count = 0;
            for (Object item : views) {
                JSONObject view = toJsonObject(item);
                if (view == null || view.get("name") == null || view.get("id") == null) {
                    continue;
                }
                String name = String.valueOf(view.get("name"));
                if (!name.toLowerCase(Locale.US).startsWith(normalizedPrefix)) {
                    continue;
                }
                syncViewToBrowserCache(view);
                cacheFullView(name, view);
                count++;
            }
            Logger.logConsoleMessage("GraphQL prefetch: cached " + count + " views with prefix '" + prefix + "'");
            return count;
        } catch (Exception e) {
            Logger.logConsoleMessage("GraphQL prefetch failed for prefix '" + prefix + "': " + e.getMessage());
            return 0;
        }
    }

    public static boolean renameTableView(String currentName, String newName) {
        try {
            installAuthCaptureHook();
            JSONObject view = findViewByName(currentName);
            if (view == null || view.get("id") == null) {
                Logger.logConsoleMessage("GraphQL Java rename: view not found or missing id " + currentName);
                return false;
            }
            enrichViewColumns(view);
            JSONObject payload = buildRenamePayload(view, newName);
            gql("SaveTableView", SAVE_TABLE_VIEW, wrapItem(payload));
            cacheViewId(newName, String.valueOf(view.get("id")));
            VIEW_ID_BY_NAME.remove(normalizeViewName(currentName));
            JSONObject renamed = buildRenamePayload(view, newName);
            syncViewToBrowserCache(renamed);
            return true;
        } catch (Exception e) {
            String detail = e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage());
            if (e.getCause() != null) {
                detail += " cause=" + e.getCause().getClass().getSimpleName() + ": "
                        + String.valueOf(e.getCause().getMessage());
            }
            Logger.logConsoleMessage("GraphQL Java rename failed: " + detail);
            return false;
        }
    }

    /** Saves a table view via JVM GraphQL using Manage Columns panel checkbox state when open. */
    public static boolean saveTableViewViaGraphql(String viewName, String... requiredColumnLabels) {
        try {
            installAuthCaptureHook();
            refreshAuthFromUi();
            return saveViewViaJavaGraphql(viewName, requiredColumnLabels);
        } catch (Exception e) {
            Logger.logConsoleMessage("GraphQL saveTableView failed for " + viewName + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean cacheViewFromGraphql(String viewName) {
        if (hasCachedViewId(viewName) && fullViewFromCache(viewName) != null) {
            syncViewToBrowserCache(fullViewFromCache(viewName));
            return true;
        }
        try {
            installAuthCaptureHook();
            JSONObject view = findViewByName(viewName);
            if (view != null && view.get("id") != null) {
                syncViewToBrowserCache(view);
                return true;
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("GraphQL cache view failed for " + viewName + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * After a UI save, resolve the backend view id via cache, browser stores, TableViews, or Java SaveTableView.
     */
    public static boolean syncViewIdAfterUiSave(String viewName) {
        try {
            JSONObject fullCached = fullViewFromCache(viewName);
            if (fullCached != null && fullCached.get("columns") != null) {
                syncViewToBrowserCache(fullCached);
                return true;
            }
            JSONObject fromJavaCache = viewFromJavaCache(viewName);
            if (fromJavaCache != null) {
                syncViewToBrowserCache(fromJavaCache);
                return true;
            }
            installAuthCaptureHook();
            refreshAuthFromUi();
            JSONObject cached = lookupCachedView(viewName);
            if (cached != null) {
                enrichViewId(cached, viewName);
                if (cached.get("id") != null) {
                    syncViewToBrowserCache(cached);
                    return true;
                }
            }
            if (resolveViewFromBrowserStores(viewName)) {
                return true;
            }
            JSONObject vars = buildTableViewsVariables();
            for (String user : resolveTableViewsUserCandidates()) {
                JSONObject altVars = new JSONObject();
                altVars.put("user", user);
                altVars.put("tableType", vars.getOrDefault("tableType", "ORDER"));
                JSONObject matched = queryTableViewsForName(viewName, altVars, null);
                if (matched != null && matched.get("id") != null) {
                    return true;
                }
            }
            return saveViewViaJavaGraphql(viewName);
        } catch (Exception e) {
            Logger.logConsoleMessage("GraphQL syncViewIdAfterUiSave failed for " + viewName + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteTableView(String viewName) {
        try {
            installAuthCaptureHook();
            JSONObject view = findViewByName(viewName);
            if (view == null || view.get("id") == null) {
                Logger.logConsoleMessage("GraphQL Java delete: view not found " + viewName);
                return false;
            }
            JSONObject vars = new JSONObject();
            vars.put("id", view.get("id"));
            gql("DeleteTableView", DELETE_TABLE_VIEW, vars);
            VIEW_ID_BY_NAME.remove(normalizeViewName(viewName));
            return true;
        } catch (Exception e) {
            String detail = e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage());
            if (e.getCause() != null) {
                detail += " cause=" + e.getCause().getClass().getSimpleName() + ": "
                        + String.valueOf(e.getCause().getMessage());
            }
            Logger.logConsoleMessage("GraphQL Java delete failed: " + detail);
            return false;
        }
    }

    private static final String FIND_VIEW_FROM_CACHE_JS =
            "function norm(v){return (v||'').replace(/\\s+/g,' ').trim();}"
                    + "function viewFromGqlResponses(name){"
                    + "  var n=norm(name);"
                    + "  var responses=window.__ffGqlResponses||[];"
                    + "  for(var i=responses.length-1;i>=0;i--){"
                    + "    var entry=responses[i];"
                    + "    var json=entry&&entry.res;"
                    + "    if(!json||!json.data)continue;"
                    + "    var saved=json.data.saveTableView||json.data.updateTableView;"
                    + "    if(saved&&saved.id&&norm(saved.name)===n)return saved;"
                    + "    var views=json.data.tableViews;"
                    + "    if(Array.isArray(views)){"
                    + "      for(var j=0;j<views.length;j++){"
                    + "        if(views[j]&&views[j].id&&norm(views[j].name)===n)return views[j];"
                    + "      }"
                    + "    }"
                    + "    if(entry.req){"
                    + "      try{"
                    + "        var body=typeof entry.req==='string'?JSON.parse(entry.req):entry.req;"
                    + "        if(body.variables&&body.variables.item&&norm(body.variables.item.name)===n){"
                    + "          var item=Object.assign({},body.variables.item);"
                    + "          if(saved&&saved.id)item.id=saved.id;"
                    + "          if(item.id)return item;"
                    + "        }"
                    + "      }catch(e){}"
                    + "    }"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function findViewInApollo(name){"
                    + "  var n=norm(name);"
                    + "  function walk(node,depth){"
                    + "    if(!node||depth>10)return null;"
                    + "    if(typeof node!=='object')return null;"
                    + "    if(node.__typename==='TableView'&&node.name&&norm(node.name)===n&&(node.id||node.__ref)){"
                    + "      return node;"
                    + "    }"
                    + "    if(node.name&&norm(node.name)===n&&(node.id||node.tableViewId)){return node;}"
                    + "    if(Array.isArray(node)){"
                    + "      for(var i=0;i<node.length;i++){var v=walk(node[i],depth+1);if(v)return v;}"
                    + "      return null;"
                    + "    }"
                    + "    for(var k in node){if(Object.prototype.hasOwnProperty.call(node,k)){"
                    + "      var v=walk(node[k],depth+1);if(v)return v;"
                    + "    }}"
                    + "    return null;"
                    + "  }"
                    + "  try{"
                    + "    if(window.__APOLLO_CLIENT__&&window.__APOLLO_CLIENT__.cache){"
                    + "      var data=window.__APOLLO_CLIENT__.cache.extract();"
                    + "      var found=walk(data,0);"
                    + "      if(found){"
                    + "        if(found.__ref&&data[found.__ref])found=data[found.__ref];"
                    + "        if(found.id||found.tableViewId)return found;"
                    + "      }"
                    + "    }"
                    + "  }catch(e){}"
                    + "  return null;"
                    + "}"
                    + "function findView(name){"
                    + "  var n=norm(name);"
                    + "  if(window.__ffLastSavedView&&norm(window.__ffLastSavedView.name)===n){"
                    + "    return window.__ffLastSavedView;"
                    + "  }"
                    + "  if(window.__ffLastSaveTableViewResponse&&norm(window.__ffLastSaveTableViewResponse.name)===n){"
                    + "    return window.__ffLastSaveTableViewResponse;"
                    + "  }"
                    + "  if(window.__ffSavedViews){"
                    + "    for(var k in window.__ffSavedViews){"
                    + "      if(norm(k)===n)return window.__ffSavedViews[k];"
                    + "    }"
                    + "  }"
                    + "  var fromGql=viewFromGqlResponses(n);"
                    + "  if(fromGql)return fromGql;"
                    + "  var fromApollo=findViewInApollo(n);"
                    + "  if(fromApollo)return fromApollo;"
                    + "  var calls=window.__ffApiCalls||[];"
                    + "  for(var i=calls.length-1;i>=0;i--){"
                    + "    var c=calls[i];"
                    + "    if(!c.body)continue;"
                    + "    try{"
                    + "      var b=JSON.parse(c.body);"
                    + "      if(b.variables&&b.variables.item&&norm(b.variables.item.name)===n){"
                    + "        return b.variables.item;"
                    + "      }"
                    + "    }catch(e){}"
                    + "  }"
                    + "  return null;"
                    + "}";

    private static JSONObject findViewByName(String name) throws Exception {
        JSONObject fromJavaCache = viewFromJavaCache(name);
        if (fromJavaCache != null) {
            return fromJavaCache;
        }
        refreshAuthFromUi();
        JSONObject fromCache = toJsonObject(runScript(FIND_VIEW_FROM_CACHE_JS + "return findView('"
                + escJs(name) + "');"));
        if (fromCache != null) {
            enrichViewId(fromCache, name);
            if (fromCache.get("id") != null) {
                return fromCache;
            }
            FulfillmentJsUtil.waitForSavedViewWithId(name, 8);
            fromCache = toJsonObject(runScript(FIND_VIEW_FROM_CACHE_JS + "return findView('"
                    + escJs(name) + "');"));
            if (fromCache != null) {
                enrichViewId(fromCache, name);
                if (fromCache.get("id") != null) {
                    return fromCache;
                }
            }
        }
        JSONObject vars = buildTableViewsVariables();
        JSONObject matched = queryTableViewsForName(name, vars, fromCache);
        if (matched != null) {
            return matched;
        }
        for (String user : resolveTableViewsUserCandidates()) {
            if (user.equals(String.valueOf(vars.get("user")))) {
                continue;
            }
            JSONObject altVars = new JSONObject();
            altVars.put("user", user);
            altVars.put("tableType", vars.getOrDefault("tableType", "ORDER"));
            matched = queryTableViewsForName(name, altVars, fromCache);
            if (matched != null) {
                return matched;
            }
        }
        return fromCache;
    }

    private static JSONObject queryTableViewsForName(String name, JSONObject vars, JSONObject fromCache)
            throws Exception {
        Logger.logConsoleMessage("GraphQL TableViews vars: user=" + vars.get("user")
                + " tableType=" + vars.get("tableType"));
        JSONObject res = gql("TableViews", resolveTableViewsQuery(), vars);
        JSONObject data = (JSONObject) res.get("data");
        if (data == null) {
            return null;
        }
        JSONArray views = (JSONArray) data.get("tableViews");
        if (views == null) {
            return null;
        }
        JSONObject matched = findViewInArray(views, name);
        if (matched != null) {
            syncViewToBrowserCache(matched);
            return matched;
        }
        StringBuilder names = new StringBuilder();
        for (Object o : views) {
            JSONObject v = toJsonObject(o);
            if (v != null && v.get("name") != null) {
                if (names.length() > 0) {
                    names.append(", ");
                }
                names.append(v.get("name"));
            }
        }
        Logger.logConsoleMessage("GraphQL TableViews: " + views.size()
                + " views returned [" + names + "]; no match for '" + name + "'");
        return null;
    }

    private static JSONObject lookupCachedView(String viewName) {
        return toJsonObject(runScript(FIND_VIEW_FROM_CACHE_JS + "return findView('"
                + escJs(viewName) + "');"));
    }

    private static boolean resolveViewFromBrowserStores(String viewName) {
        JSONObject view = lookupCachedView(viewName);
        if (view != null && view.get("id") != null) {
            syncViewToBrowserCache(view);
            return true;
        }
        enrichViewId(view, viewName);
        if (view != null && view.get("id") != null) {
            syncViewToBrowserCache(view);
            return true;
        }
        return false;
    }

    public static void cacheViewId(String viewName, String id) {
        if (viewName != null && id != null) {
            VIEW_ID_BY_NAME.put(normalizeViewName(viewName), id);
        }
    }

    private static void cacheFullView(String viewName, JSONObject fullView) {
        if (viewName != null && fullView != null) {
            FULL_VIEW_BY_NAME.put(normalizeViewName(viewName), fullView);
        }
    }

    private static JSONObject fullViewFromCache(String viewName) {
        if (viewName == null) {
            return null;
        }
        return FULL_VIEW_BY_NAME.get(normalizeViewName(viewName));
    }

    private static String normalizeViewName(String viewName) {
        return viewName.replaceAll("\\s+", " ").trim().toLowerCase();
    }

    private static JSONObject viewFromJavaCache(String viewName) {
        String id = VIEW_ID_BY_NAME.get(normalizeViewName(viewName));
        if (id == null) {
            return null;
        }
        JSONObject view = new JSONObject();
        view.put("id", id);
        view.put("name", viewName);
        view.put("tableType", "ORDER");
        return view;
    }

    private static JSONObject fetchStandardViewTemplate() throws Exception {
        refreshAuthFromUi();
        JSONObject vars = buildTableViewsVariables();
        JSONObject res = gql("TableViews", resolveTableViewsQuery(), vars);
        JSONObject data = (JSONObject) res.get("data");
        if (data == null) {
            return null;
        }
        JSONArray views = (JSONArray) data.get("tableViews");
        if (views == null || views.isEmpty()) {
            return null;
        }
        JSONObject standard = findViewInArray(views, "Standard View");
        return standard != null ? standard : toJsonObject(views.get(0));
    }

    public static boolean isOrderColumnLabelAvailable(String columnLabel) {
        if (columnLabel == null || columnLabel.trim().isEmpty()) {
            return false;
        }
        try {
            installAuthCaptureHook();
            JSONObject template = fetchStandardViewTemplate();
            if (template == null || template.get("columns") == null) {
                return false;
            }
            JSONObject columns = toJsonObject(template.get("columns"));
            if (columns == null) {
                return false;
            }
            Object orderTable = columns.get("orderTable");
            if (!(orderTable instanceof JSONArray)) {
                return false;
            }
            JSONArray orderColumns = (JSONArray) orderTable;
            String target = normalizeColumnLabel(columnLabel);
            for (Object item : orderColumns) {
                JSONObject col = toJsonObject(item);
                if (col == null || col.get("label") == null) {
                    continue;
                }
                String label = normalizeColumnLabel(String.valueOf(col.get("label")));
                if (label.equals(target) || namesMatch(columnLabel, String.valueOf(col.get("label")))) {
                    return true;
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("GraphQL order column lookup failed for '" + columnLabel + "': " + e.getMessage());
        }
        return false;
    }

    public static boolean hasCachedViewId(String viewName) {
        return viewName != null && VIEW_ID_BY_NAME.containsKey(normalizeViewName(viewName));
    }

    /** Selected order-column labels from a GraphQL-cached full view payload. */
    @SuppressWarnings("unchecked")
    public static String[] getCachedSelectedOrderColumnLabels(String viewName) {
        JSONObject full = FULL_VIEW_BY_NAME.get(normalizeViewName(viewName));
        if (full == null || full.get("columns") == null) {
            return new String[0];
        }
        JSONObject columns = toJsonObject(full.get("columns"));
        if (columns == null) {
            return new String[0];
        }
        Object orderTable = columns.get("orderTable");
        if (!(orderTable instanceof JSONArray)) {
            return new String[0];
        }
        java.util.List<String> labels = new java.util.ArrayList<>();
        for (Object item : (JSONArray) orderTable) {
            JSONObject col = toJsonObject(item);
            if (col == null || col.get("label") == null) {
                continue;
            }
            Object selected = col.get("selected");
            if (Boolean.TRUE.equals(selected) || "true".equalsIgnoreCase(String.valueOf(selected))) {
                labels.add(String.valueOf(col.get("label")));
            }
        }
        return labels.toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    private static JSONObject buildSavePayloadFromTemplate(String viewName, JSONObject template,
            String... requiredColumnLabels) {
        JSONObject payload = new JSONObject();
        payload.put("name", viewName);
        payload.put("tableType", template.getOrDefault("tableType", "ORDER"));
        JSONObject uiColumns = captureColumnsFromManageColumnsPanel();
        JSONObject templateColumns = toJsonObject(template.get("columns"));
        if (templateColumns != null && !templateColumns.isEmpty()) {
            payload.put("columns", mergeColumnsWithTemplate(templateColumns, uiColumns, requiredColumnLabels));
            Logger.logConsoleMessage("GraphQL Java save: merged template + Manage Columns for " + viewName);
        } else if (uiColumns != null && !uiColumns.isEmpty()) {
            payload.put("columns", sanitizeColumns(uiColumns));
            Logger.logConsoleMessage("GraphQL Java save: using Manage Columns panel selection for " + viewName);
        } else if (template.get("columns") != null) {
            Object columns = template.get("columns");
            JSONObject columnsCopy = columns instanceof JSONObject
                    ? deepJsonObject((java.util.Map<String, Object>) columns)
                    : deepJsonObject((java.util.Map<String, Object>) columns);
            stripTypename(columnsCopy);
            payload.put("columns", sanitizeColumns(columnsCopy));
        }
        stripTypename(payload);
        return payload;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject mergeColumnsWithTemplate(JSONObject templateColumns, JSONObject uiColumns,
            String... requiredColumnLabels) {
        JSONObject uiBySection = uiColumns != null ? uiColumns : new JSONObject();
        JSONObject merged = new JSONObject();
        for (Object sectionKey : templateColumns.keySet()) {
            Object sectionVal = templateColumns.get(sectionKey);
            if (!(sectionVal instanceof JSONArray)) {
                continue;
            }
            JSONArray templateArr = (JSONArray) sectionVal;
            JSONArray uiArr = uiBySection.get(sectionKey) instanceof JSONArray
                    ? (JSONArray) uiBySection.get(sectionKey) : new JSONArray();
            java.util.Map<String, Boolean> uiSelected = new java.util.HashMap<>();
            for (Object item : uiArr) {
                JSONObject col = toJsonObject(item);
                if (col == null || col.get("label") == null) {
                    continue;
                }
                Object selected = col.get("selected");
                uiSelected.put(normalizeColumnLabel(String.valueOf(col.get("label"))),
                        selected instanceof Boolean ? (Boolean) selected : Boolean.parseBoolean(String.valueOf(selected)));
            }
            JSONArray out = new JSONArray();
            for (Object item : templateArr) {
                JSONObject col = toJsonObject(item);
                if (col == null || col.get("label") == null) {
                    continue;
                }
                String label = String.valueOf(col.get("label"));
                boolean selected;
                if (requiredColumnLabels != null && requiredColumnLabels.length > 0) {
                    selected = false;
                    for (String required : requiredColumnLabels) {
                        if (required != null && namesMatch(required, label)) {
                            selected = true;
                            break;
                        }
                    }
                } else {
                    selected = uiSelected.containsKey(normalizeColumnLabel(label))
                            ? uiSelected.get(normalizeColumnLabel(label))
                            : Boolean.TRUE.equals(col.get("selected"));
                    for (String required : requiredColumnLabels) {
                        if (required != null && namesMatch(required, label)) {
                            selected = true;
                            break;
                        }
                    }
                }
                JSONObject slim = new JSONObject();
                slim.put("label", label);
                slim.put("selected", selected);
                out.add(slim);
            }
            merged.put(String.valueOf(sectionKey), out);
        }
        return sanitizeColumns(merged);
    }

    private static String normalizeColumnLabel(String label) {
        return label == null ? "" : label.replaceAll("\\s+", " ").trim().toLowerCase();
    }

    private static JSONObject captureColumnsFromManageColumnsPanel() {
        try {
            String json = FulfillmentJsUtil.captureManageColumnsSelectionJson();
            if (json == null || json.trim().isEmpty() || "null".equalsIgnoreCase(json.trim())) {
                return null;
            }
            JSONObject root = (JSONObject) new JSONParser().parse(json);
            Object cols = root.get("columns");
            return cols instanceof JSONObject ? (JSONObject) cols : null;
        } catch (Exception e) {
            Logger.logConsoleMessage("Capture UI columns failed: " + e.getMessage());
            return null;
        }
    }

    private static JSONObject sanitizeColumns(JSONObject columns) {
        JSONObject out = new JSONObject();
        if (columns == null) {
            return out;
        }
        for (Object sectionKey : columns.keySet()) {
            Object sectionVal = columns.get(sectionKey);
            if (!(sectionVal instanceof JSONArray)) {
                continue;
            }
            JSONArray arr = (JSONArray) sectionVal;
            JSONArray cleaned = new JSONArray();
            for (Object item : arr) {
                JSONObject col = toJsonObject(item);
                if (col == null) {
                    continue;
                }
                JSONObject slim = new JSONObject();
                if (col.get("label") != null) {
                    slim.put("label", col.get("label"));
                }
                if (col.get("selected") != null) {
                    slim.put("selected", col.get("selected"));
                }
                if (!slim.isEmpty()) {
                    cleaned.add(slim);
                }
            }
            out.put(String.valueOf(sectionKey), cleaned);
        }
        return out;
    }

    private static JSONObject extractMutationView(JSONObject response, String field) {
        if (response == null) {
            return null;
        }
        JSONObject data = (JSONObject) response.get("data");
        if (data == null) {
            return null;
        }
        return toJsonObject(data.get(field));
    }

    private static boolean saveViewViaJavaGraphql(String viewName, String... requiredColumnLabels) throws Exception {
        JSONObject existingFull = fullViewFromCache(viewName);
        if (existingFull != null && existingFull.get("columns") != null) {
            syncViewToBrowserCache(existingFull);
            Logger.logConsoleMessage("GraphQL Java save: reused cached full view for " + viewName);
            return true;
        }
        JSONObject template = fetchStandardViewTemplate();
        if (template == null) {
            Logger.logConsoleMessage("GraphQL Java save: no template view for " + viewName);
            return false;
        }
        JSONObject payload = buildSavePayloadFromTemplate(viewName, template, requiredColumnLabels);
        logPayloadColumnSelection(payload, requiredColumnLabels);
        if (FulfillmentJsUtil.saveTableViewItemViaBrowserGraphql(payload)) {
            JSONObject fullView = cloneJsonObject(payload);
            Object cachedId = VIEW_ID_BY_NAME.get(normalizeViewName(viewName));
            if (cachedId != null) {
                fullView.put("id", cachedId);
            }
            cacheFullView(viewName, fullView);
            Logger.logConsoleMessage("GraphQL browser save: id cached for " + viewName);
            return true;
        }
        try {
            JSONObject res = gql("SaveTableView", SAVE_TABLE_VIEW, wrapItem(payload));
            JSONObject saved = extractMutationView(res, "saveTableView");
            if (saved != null && saved.get("id") != null) {
                JSONObject fullView = cloneJsonObject(payload);
                fullView.put("id", saved.get("id"));
                fullView.put("name", viewName);
                cacheFullView(viewName, fullView);
                syncViewToBrowserCache(fullView);
                injectGraphqlSaveIntoBrowser(fullView);
                Logger.logConsoleMessage("GraphQL Java save: id=" + saved.get("id") + " name=" + viewName);
                return true;
            }
        } catch (Exception saveEx) {
            String msg = String.valueOf(saveEx.getMessage()).toLowerCase();
            Logger.logConsoleMessage("GraphQL Java save attempt for " + viewName + ": " + saveEx.getMessage()
                    + " payloadKeys=" + payload.keySet());
            if (msg.contains("exist") || msg.contains("duplicate") || msg.contains("already")) {
                return resolveViewFromBrowserStores(viewName);
            }
            throw saveEx;
        }
        return false;
    }

    private static void syncViewToBrowserCache(JSONObject view) {
        if (view == null || view.get("name") == null) {
            return;
        }
        if (view.get("id") != null) {
            cacheViewId(String.valueOf(view.get("name")), String.valueOf(view.get("id")));
        }
        try {
            String viewJson = view.toJSONString().replace("\\", "\\\\").replace("'", "\\'");
            runScript("try{"
                    + "var v=JSON.parse('" + viewJson + "');"
                    + "if(!window.__ffSavedViews)window.__ffSavedViews={};"
                    + "window.__ffSavedViews[v.name]=v;"
                    + "window.__ffLastSavedView=v;"
                    + "window.__ffLastSaveTableViewResponse=v;"
                    + "}catch(e){}");
        } catch (Exception ignored) {
        }
    }

    private static void injectGraphqlSaveIntoBrowser(JSONObject fullView) {
        if (fullView == null || fullView.get("name") == null) {
            return;
        }
        try {
            String viewJson = fullView.toJSONString().replace("\\", "\\\\").replace("'", "\\'");
            runScript("try{"
                    + "var item=JSON.parse('" + viewJson + "');"
                    + "var req={operationName:'SaveTableView',variables:{item:item}};"
                    + "var res={data:{saveTableView:{id:item.id,name:item.name,__typename:'TableView'}}};"
                    + "if(!window.__ffGqlResponses)window.__ffGqlResponses=[];"
                    + "window.__ffGqlResponses.push({req:JSON.stringify(req),res:res,t:Date.now()});"
                    + "if(!window.__ffApiCalls)window.__ffApiCalls=[];"
                    + "window.__ffApiCalls.push({method:'POST',url:'graphql',body:JSON.stringify(req),t:Date.now()});"
                    + "}catch(e){}");
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private static JSONObject cloneJsonObject(JSONObject source) {
        if (source == null) {
            return new JSONObject();
        }
        return deepJsonObject((java.util.Map<String, Object>) source);
    }

    private static void logPayloadColumnSelection(JSONObject payload, String... requiredColumnLabels) {
        if (payload == null || payload.get("columns") == null) {
            return;
        }
        JSONObject columns = toJsonObject(payload.get("columns"));
        if (columns == null) {
            return;
        }
        Object orderTable = columns.get("orderTable");
        if (!(orderTable instanceof JSONArray)) {
            return;
        }
        JSONArray arr = (JSONArray) orderTable;
        int selectedCount = 0;
        StringBuilder selectedLabels = new StringBuilder();
        for (Object item : arr) {
            JSONObject col = toJsonObject(item);
            if (col == null || !Boolean.TRUE.equals(col.get("selected"))) {
                continue;
            }
            selectedCount++;
            if (selectedLabels.length() > 0) {
                selectedLabels.append(", ");
            }
            selectedLabels.append(col.get("label"));
        }
        Logger.logConsoleMessage("GraphQL save payload: " + selectedCount + " order columns selected ["
                + selectedLabels + "] required=" + java.util.Arrays.toString(requiredColumnLabels));
        for (String required : requiredColumnLabels) {
            if (required == null) {
                continue;
            }
            boolean found = false;
            for (Object item : arr) {
                JSONObject col = toJsonObject(item);
                if (col != null && namesMatch(required, String.valueOf(col.get("label")))
                        && Boolean.TRUE.equals(col.get("selected"))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Logger.logConsoleMessage("GraphQL save payload WARNING: required column not selected: " + required);
            }
        }
    }

    private static void enrichViewId(JSONObject view, String name) {
        if (view == null || view.get("id") != null) {
            return;
        }
        Object domId = runScript(FulfillmentJsUtil.viewIdLookupJs(name));
        if (domId != null && !String.valueOf(domId).trim().isEmpty()
                && !"null".equals(String.valueOf(domId))) {
            view.put("id", String.valueOf(domId).trim());
        }
    }

    private static void enrichViewColumns(JSONObject view) {
        if (view == null || view.get("columns") != null) {
            return;
        }
        try {
            JSONObject template = fetchStandardViewTemplate();
            if (template != null && template.get("columns") != null) {
                view.put("columns", template.get("columns"));
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("GraphQL enrich columns failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static JSONObject buildRenamePayload(JSONObject view, String newName) {
        JSONObject payload = new JSONObject();
        if (view.get("id") != null) {
            payload.put("id", view.get("id"));
        }
        payload.put("name", newName);
        payload.put("tableType", view.getOrDefault("tableType", "ORDER"));
        if (view.get("columns") != null) {
            Object columns = view.get("columns");
            JSONObject columnsCopy = columns instanceof JSONObject
                    ? deepJsonObject((java.util.Map<String, Object>) columns)
                    : deepJsonObject((java.util.Map<String, Object>) columns);
            stripTypename(columnsCopy);
            payload.put("columns", sanitizeColumns(columnsCopy));
        }
        stripTypename(payload);
        return payload;
    }

    private static boolean namesMatch(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return expected.replaceAll("\\s+", " ").trim()
                .equalsIgnoreCase(actual.replaceAll("\\s+", " ").trim());
    }

    private static JSONObject findViewInArray(JSONArray views, String name) {
        if (views == null) {
            return null;
        }
        for (Object o : views) {
            JSONObject v = toJsonObject(o);
            if (v == null) {
                continue;
            }
            if (namesMatch(name, String.valueOf(v.get("name")))) {
                return v;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject toJsonObject(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }
        if (value instanceof java.util.Map) {
            return deepJsonObject((java.util.Map<String, Object>) value);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject deepJsonObject(java.util.Map<String, Object> map) {
        JSONObject out = new JSONObject();
        for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof java.util.Map) {
                out.put(entry.getKey(), deepJsonObject((java.util.Map<String, Object>) val));
            } else if (val instanceof java.util.List) {
                out.put(entry.getKey(), deepJsonArray((java.util.List<Object>) val));
            } else {
                out.put(entry.getKey(), val);
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static JSONArray deepJsonArray(java.util.List<Object> list) {
        JSONArray out = new JSONArray();
        for (Object val : list) {
            if (val instanceof java.util.Map) {
                out.add(deepJsonObject((java.util.Map<String, Object>) val));
            } else if (val instanceof java.util.List) {
                out.add(deepJsonArray((java.util.List<Object>) val));
            } else {
                out.add(val);
            }
        }
        return out;
    }

    private static void refreshAuthFromUi() throws InterruptedException {
        runScript(
                "try{"
                        + "var d=document.querySelector('.dropdown-reset-container');"
                        + "if(d){d.click();return true;}"
                        + "var btn=document.querySelector('#tableViewButton');"
                        + "if(btn){btn.click();return true;}"
                        + "}catch(e){}"
                        + "return false;");
        for (int i = 0; i < 10; i++) {
            Object captured = runScript("return window.__ffTableViewsVars&&window.__ffTableViewsVars.user;");
            if (captured != null && !String.valueOf(captured).trim().isEmpty()
                    && !"null".equals(String.valueOf(captured))) {
                break;
            }
            Thread.sleep(500);
        }
    }

    private static void stripTypename(JSONObject obj) {
        if (obj == null) {
            return;
        }
        obj.remove("__typename");
        for (Object key : obj.keySet()) {
            Object val = obj.get(key);
            if (val instanceof JSONObject) {
                stripTypename((JSONObject) val);
            } else if (val instanceof JSONArray) {
                stripTypenameArray((JSONArray) val);
            }
        }
    }

    private static void stripTypenameArray(JSONArray arr) {
        if (arr == null) {
            return;
        }
        for (Object item : arr) {
            if (item instanceof JSONObject) {
                stripTypename((JSONObject) item);
            } else if (item instanceof JSONArray) {
                stripTypenameArray((JSONArray) item);
            }
        }
    }

    private static JSONObject wrapItem(JSONObject view) {
        JSONObject vars = new JSONObject();
        vars.put("item", view);
        return vars;
    }

    private static String resolveTableViewsQuery() {
        return TABLE_VIEWS_QUERY;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject buildTableViewsVariables() {
        Object captured = runScript("return window.__ffTableViewsVars;");
        if (captured instanceof java.util.Map) {
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) captured;
            JSONObject vars = new JSONObject();
            for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
                vars.put(entry.getKey(), entry.getValue());
            }
            if (vars.get("tableType") != null) {
                vars.put("user", resolveTableViewsUser());
                return vars;
            }
        }
        JSONObject vars = new JSONObject();
        vars.put("user", resolveTableViewsUser());
        vars.put("tableType", "ORDER");
        return vars;
    }

    private static String resolveTableViewsUser() {
        java.util.LinkedHashSet<String> candidates = resolveTableViewsUserCandidates();
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Cannot resolve user for TableViews query");
        }
        return candidates.iterator().next();
    }

    private static java.util.LinkedHashSet<String> resolveTableViewsUserCandidates() {
        java.util.LinkedHashSet<String> candidates = new java.util.LinkedHashSet<>();
        String jwtUser = decodeUserFromAuthHeader();
        if (jwtUser != null) {
            candidates.add(jwtUser);
        }
        Object captured = runScript("return window.__ffTableViewsVars&&window.__ffTableViewsVars.user;");
        String capturedUser = stringOrNull(captured);
        if (capturedUser != null) {
            candidates.add(capturedUser);
        }
        String configUser = stringOrNull(Config.getString("Username"));
        if (configUser != null) {
            candidates.add(configUser);
        }
        return candidates;
    }

    private static String decodeUserFromAuthHeader() {
        try {
            String auth = stringOrNull(runScript("return window.__ffAuthHeader;"));
            if (auth == null) {
                return null;
            }
            if (auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
                auth = auth.substring(7).trim();
            }
            String[] parts = auth.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            byte[] decoded = Base64.getUrlDecoder().decode(padBase64(parts[1]));
            JSONParser parser = new JSONParser();
            Object parsed = parser.parse(new String(decoded, StandardCharsets.UTF_8));
            if (!(parsed instanceof JSONObject)) {
                return null;
            }
            JSONObject claims = (JSONObject) parsed;
            for (String key : new String[]{"preferred_username", "email", "upn", "unique_name", "sub"}) {
                Object value = claims.get(key);
                if (value != null) {
                    String text = String.valueOf(value).trim();
                    if (!text.isEmpty()) {
                        return text;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String padBase64(String value) {
        int mod = value.length() % 4;
        if (mod == 0) {
            return value;
        }
        StringBuilder padded = new StringBuilder(value);
        for (int i = mod; i < 4; i++) {
            padded.append('=');
        }
        return padded.toString();
    }

    @SuppressWarnings("unchecked")
    private static JSONObject gql(String operationName, String query, JSONObject variables) throws Exception {
        String authHeader = waitForCapturedAuthHeader(25000);
        Object gqlUrlObj = runScript("return window.__ffGqlUrl;");
        String gqlUrl = gqlUrlObj != null ? String.valueOf(gqlUrlObj) : GQL_URL;
        if ("null".equals(gqlUrl) || gqlUrl.isEmpty()) {
            gqlUrl = GQL_URL;
        }
        String cookies = stringOrNull(runScript("return document.cookie||'';"));
        String origin = resolveRequestOrigin();
        String referer = stringOrNull(runScript("return window.location.href;"));
        if (referer == null || referer.isEmpty() || "null".equals(referer)) {
            referer = resolveConfiguredTargetUrl();
        }
        Logger.logConsoleMessage("GraphQL " + operationName + " auth="
                + (authHeader != null ? "captured-app-header" : "missing") + " cookies="
                + (cookies != null && !cookies.isEmpty() ? "present" : "missing")
                + " origin=" + origin);
        JSONObject body = new JSONObject();
        body.put("operationName", operationName);
        body.put("query", query);
        body.put("variables", variables);
        JSONObject ext = new JSONObject();
        JSONObject lib = new JSONObject();
        lib.put("name", "@apollo/client");
        lib.put("version", "4.1.0");
        ext.put("clientLibrary", lib);
        body.put("extensions", ext);

        HttpURLConnection conn = (HttpURLConnection) new URL(gqlUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Origin", origin);
        conn.setRequestProperty("Referer", referer);
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        if (authHeader != null) {
            conn.setRequestProperty("Authorization", authHeader);
        }
        if (cookies != null && !cookies.isEmpty()) {
            conn.setRequestProperty("Cookie", cookies);
        }
        byte[] payload = body.toJSONString().getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(payload.length));
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload);
        }
        int code = conn.getResponseCode();
        InputStream stream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        String response = readStream(stream);
        if (response == null || response.isEmpty()) {
            throw new IllegalStateException("Empty GraphQL response (HTTP " + code + ")");
        }
        Logger.logConsoleMessage("GraphQL " + operationName + " HTTP " + code + " bytes="
                + response.length());
        JSONParser parser = new JSONParser();
        Object parsed;
        try {
            parsed = parser.parse(response);
        } catch (Exception parseEx) {
            String preview = response.length() > 200 ? response.substring(0, 200) : response;
            throw new IllegalStateException("GraphQL parse failed (HTTP " + code + "): " + preview, parseEx);
        }
        if (!(parsed instanceof JSONObject)) {
            throw new IllegalStateException("Unexpected GraphQL response: " + response);
        }
        JSONObject json = (JSONObject) parsed;
        if (code >= 400) {
            throw new IllegalStateException("HTTP " + code + ": " + response);
        }
        JSONArray errors = (JSONArray) json.get("errors");
        if (errors != null && !errors.isEmpty()) {
            JSONObject err = (JSONObject) errors.get(0);
            String message = String.valueOf(err.get("message"));
            Logger.logConsoleMessage("GraphQL " + operationName + " error: " + message
                    + " response=" + (response.length() > 300 ? response.substring(0, 300) : response));
            throw new IllegalStateException(message);
        }
        return json;
    }

    private static String stringOrNull(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() || "null".equals(text) ? null : text;
    }

    private static String waitForCapturedAuthHeader(long timeoutMs) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            String authHeader = stringOrNull(runScript("return window.__ffAuthHeader;"));
            if (authHeader != null) {
                return authHeader;
            }
            refreshAuthFromUi();
            FulfillmentJsUtil.openManageColumnsPanel();
            Thread.sleep(500);
        }
        throw new IllegalStateException("App GraphQL Authorization header was not captured");
    }

    private static String readStream(InputStream stream) {
        if (stream == null) {
            return "";
        }
        try (Scanner s = new Scanner(stream, StandardCharsets.UTF_8.name()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    private static String escJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private static Object runScript(String script) {
        try {
            return BaseTest.driver.get().browser().executeScript(script);
        } catch (Exception e) {
            return null;
        }
    }

    private static String resolveConfiguredTargetUrl() {
        String url = Config.getString("TargetUrl");
        if (url != null && !url.trim().isEmpty()) {
            return url.trim();
        }
        String env = Config.getString("TestEnvironment");
        if (env == null || env.trim().isEmpty()) {
            env = Config.getString("Environment");
        }
        if (env != null) {
            switch (env.trim().toLowerCase(java.util.Locale.US)) {
                case "prod":
                    url = Config.getString("TargetUrlPROD");
                    break;
                case "uat":
                    url = Config.getString("TargetUrlUAT");
                    break;
                case "pr":
                    url = Config.getString("TargetUrlPR");
                    break;
                case "dev":
                default:
                    url = Config.getString("TargetUrlDEV");
                    break;
            }
        }
        if (url == null || url.trim().isEmpty()) {
            url = "https://operationsconsole.paramountmsc.com/fulfillment/";
        }
        return url.trim();
    }

    private static String resolveRequestOrigin() {
        String origin = stringOrNull(runScript("return window.location.origin;"));
        if (origin != null && !origin.isEmpty() && !"null".equals(origin)) {
            return origin;
        }
        try {
            java.net.URL parsed = new java.net.URL(resolveConfiguredTargetUrl());
            return parsed.getProtocol() + "://" + parsed.getHost();
        } catch (Exception e) {
            return "https://operationsconsole.paramountmsc.com";
        }
    }
}
