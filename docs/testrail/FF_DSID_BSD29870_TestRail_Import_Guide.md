# BSD-29870 DSID — TestRail import

**Project:** Fulfillment Console (223)  
**Jira:** [BSD-29870](https://paramount.atlassian.net/browse/BSD-29870)

## Import

1. TestRail → **Import Cases** → CSV
2. File: `docs/testrail/FF_DSID_BSD29870_ManualCases_Import.csv`
3. Section path: `Fulfillment Console > BSD-29870 > Orders > DSID` (and Line Items)

## After import

Update `DsidTestRailCaseIds.java` with real case IDs:

```java
public static final String O_001 = "Cxxxxxxx";
public static final String O_002 = "Cxxxxxxx";
public static final String LI_001 = "Cxxxxxxx";
```

## Run automation

```bash
mvn test "-DsuiteXmlFile=src/test/resources/FF_DSID_BSD29870_ProdServerSuite.xml"
```
