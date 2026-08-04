package com.paramount.test.ff.uitests.helpers.partneroptions;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class PartnerOptionsGraphqlResponseParserTest {

    @Test
    public void preservesExactDuplicatesWithinSingleCapture() {
        JSONArray captures = new JSONArray();
        captures.add(captureWithPartnerObjects(
                "Alpha",
                "MPD Disney Spain FSP",
                "MPD Disney Spain FSP",
                "Zulu"));

        List<String> labels = PartnerOptionsGraphqlResponseParser.extractPartnerLabels(captures);

        Assert.assertEquals(labels, Arrays.asList(
                "Alpha",
                "MPD Disney Spain FSP",
                "MPD Disney Spain FSP",
                "Zulu"));
    }

    @Test
    public void dedupesScrollOverlapAcrossCapturesButKeepsWithinCaptureDuplicates() {
        JSONArray captures = new JSONArray();
        captures.add(captureWithPartnerObjects("Alpha", "Beta", "Gamma"));
        captures.add(captureWithPartnerObjects("Gamma", "Comcast Unified Nick@Nite FSP", "Comcast Unified Nick@Nite FSP"));

        List<String> labels = PartnerOptionsGraphqlResponseParser.extractPartnerLabels(captures);

        Assert.assertEquals(labels, Arrays.asList(
                "Alpha",
                "Beta",
                "Gamma",
                "Comcast Unified Nick@Nite FSP",
                "Comcast Unified Nick@Nite FSP"));
    }

    private static JSONObject captureWithPartnerObjects(String... partnerNames) {
        JSONArray partners = new JSONArray();
        for (String partnerName : partnerNames) {
            JSONObject partner = new JSONObject();
            partner.put("partnerName", partnerName);
            partners.add(partner);
        }

        JSONObject data = new JSONObject();
        data.put("partners", partners);

        JSONObject response = new JSONObject();
        response.put("data", data);

        JSONObject capture = new JSONObject();
        capture.put("request", "query partnerOptions");
        capture.put("response", response.toJSONString());
        return capture;
    }
}
