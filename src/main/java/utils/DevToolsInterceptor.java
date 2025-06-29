package utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v134.network.Network;
import org.openqa.selenium.devtools.v134.network.model.Request;
import org.openqa.selenium.devtools.v134.network.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class DevToolsInterceptor {
    private final DevTools devTools;
    private static final List<JSONObject> capturedPayloads = new ArrayList<>();
    private final AtomicLong lastApiReceivedAt;
    private final int DEBOUNCE_TIMEOUT_MS = 2000;
    private static final Logger logger = LoggerFactory.getLogger(DevToolsInterceptor.class);
    static Map<Long, Integer> apiCallsPerSecond = new TreeMap<>();

    public DevToolsInterceptor(DevTools devTools) {
        this.devTools = devTools;
        this.lastApiReceivedAt = new AtomicLong(System.currentTimeMillis());

        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        devTools.addListener(Network.requestWillBeSent(), request -> {
            Request req = request.getRequest();
            String url = req.getUrl();
            Optional<ResourceType> resourceTypeOpt = request.getType();

            // Check if resourceType is present
            if (resourceTypeOpt.isPresent()) {
                ResourceType resourceType = resourceTypeOpt.get();

                // Correct condition: (XHR or SCRIPT) AND URL contains "/ss"
                if ((resourceType == ResourceType.XHR || resourceType == ResourceType.SCRIPT) && url.contains("/ss")) {
                    System.out.println("Captured " + resourceType + " request to URL: " + url);
                    logger.atInfo().log("Captured " + resourceType + " request to URL: " + url);
                    lastApiReceivedAt.set(System.currentTimeMillis());
                    long timestampSec = Instant.now().getEpochSecond();
                    apiCallsPerSecond.put(timestampSec, apiCallsPerSecond.getOrDefault(timestampSec, 0) + 1);
                    JSONObject payload = new JSONObject();
                    payload.put("url", url);
                    payload.put("method", req.getMethod());
                    payload.put("timestamp", System.currentTimeMillis());  // <-- ADD THIS




                    // Parse query parameters from URL and put them in JSON
                    JSONObject queryParams = parseQueryParamsFromUrl(url);
                    payload.put("queryParams", queryParams);
                    logger.atInfo().log("payload==="+payload);
                    System.out.println("payload==="+payload);

                    Optional<String> postDataOpt = req.getPostData();
                    if (postDataOpt.isPresent()) {
                        String decoded = URLDecoder.decode(postDataOpt.get(), StandardCharsets.UTF_8);
                        JSONObject postBodyJson = parsePayloadToJson(decoded);
                        payload.put("postBody", postBodyJson);
                    }

                    capturedPayloads.add(payload);
                    System.out.println("Total captured API calls so far: " + capturedPayloads.size());

                }
            }
        });
    }

    public long getLastApiReceivedAt() {
        return lastApiReceivedAt.get();
    }

    public void exportCapturedPayloadsToJson(String filePath) {
        try (FileWriter file = new FileWriter(filePath)) {
            JSONArray arr = new JSONArray(capturedPayloads);
            file.write(arr.toString(10));
            System.out.println("Captured payloads saved to " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void validateAdobeEvents(int[] expectedEventIds) {
//        Set<Integer> foundEvents = new HashSet<>();
//
//        for (JSONObject payload : capturedPayloads) {
//            if (payload.has("postBody")) {
//                JSONObject postBody = payload.getJSONObject("postBody");
//                for (int eventId : expectedEventIds) {
//                    String key = "event" + eventId;
//                    if (postBody.has(key)) {
//                        foundEvents.add(eventId);
//                    }
//                }
//            }
//        }
//
//        for (int eventId : expectedEventIds) {
//            if (!foundEvents.contains(eventId)) {
//                throw new AssertionError("Expected Adobe Analytics event " + eventId + " was NOT triggered.");
//            }
//        }
//
//        System.out.println("All expected Adobe Analytics events triggered: " + Arrays.toString(expectedEventIds));
//    }

    private JSONObject parsePayloadToJson(String payload) {
        JSONObject json = new JSONObject();
        String[] pairs = payload.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                json.put(key, value);
            }
        }
        return json;
    }

    private JSONObject parseQueryParamsFromUrl(String url) {
        JSONObject json = new JSONObject();
        try {
            String query = new java.net.URL(url).getQuery();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] parts = pair.split("=", 2);
                    if (parts.length == 2) {
                        String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                        json.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

//    public void validateAdobeEvents(int expectedEventIds) {
//        Set<Integer> foundEvents = new HashSet<>();
//
//        for (JSONObject payload : capturedPayloads) {
//            // Check postBody if present
//            if (payload.has("postBody")) {
//                JSONObject postBody = payload.getJSONObject("postBody");
//                for (int eventId : expectedEventIds) {
//                    String key = "event" + eventId;
//                    if (postBody.has(key)) {
//                        foundEvents.add(eventId);
//                    }
//                }
//            }
//
//            // Also check queryParams for eventXXX presence in 'events' param
//            if (payload.has("queryParams")) {
//                JSONObject queryParams = payload.getJSONObject("queryParams");
//                if (queryParams.has("events")) {
//                    String eventsStr = queryParams.getString("events"); // e.g. "event193,event194"
//                    for (int eventId : expectedEventIds) {
//                        String expectedEvent = "event" + eventId;
//                        if (eventsStr.contains(expectedEvent)) {
//                            foundEvents.add(eventId);
//                        }
//                    }
//                }
//            }
//        }
//
//        for (int eventId : expectedEventIds) {
//            if (!foundEvents.contains(eventId)) {
//                throw new AssertionError("Expected Adobe Analytics event " + eventId + " was NOT triggered.");
//            }
//        }
//        System.out.println("All expected Adobe Analytics events triggered: " + Arrays.toString(expectedEventIds));
//    }
//


    public void validateAdobeEvents(String expectedEventCsv) {
        Set<String> expectedEvents = new HashSet<>(Arrays.asList(expectedEventCsv.split(",")));
        Set<String> foundEvents = new HashSet<>();
        JSONArray debugLog = new JSONArray();

        int scriptOrder = 1;

        for (JSONObject payload : capturedPayloads) {
            JSONObject debugEntry = new JSONObject();
            debugEntry.put("scriptOrder", scriptOrder++);
            debugEntry.put("url", payload.optString("url", "N/A"));
            debugEntry.put("method", payload.optString("method", "N/A"));
            debugEntry.put("payload", payload);

            // Extract events from postBody or queryParams
            String eventsStr = "";

            if (payload.has("postBody")) {
                JSONObject postBody = payload.getJSONObject("postBody");
                if (postBody.has("events")) {
                    eventsStr = postBody.getString("events");
                }
            }

            if (eventsStr.isEmpty() && payload.has("queryParams")) {
                JSONObject queryParams = payload.getJSONObject("queryParams");
                if (queryParams.has("events")) {
                    eventsStr = queryParams.getString("events");
                }
            }

            // Check which expected events are found
            for (String expected : expectedEvents) {
                if (eventsStr.contains(expected)) {
                    foundEvents.add(expected);
                }
            }

            debugLog.put(debugEntry);
        }

        // Identify missing events
        Set<String> missingEvents = new HashSet<>(expectedEvents);
        missingEvents.removeAll(foundEvents);

        if (!missingEvents.isEmpty()) {
            // Save failure report to file
            JSONObject failureJson = new JSONObject();
            failureJson.put("missingEvents", missingEvents);
            failureJson.put("fullLog", debugLog);

            try (FileWriter file = new FileWriter("target/missing_adobe_events.json")) {
                file.write(failureJson.toString(4));
                System.out.println("❌ Missing event report saved to: target/missing_adobe_events.json");
            } catch (IOException e) {
                e.printStackTrace();
            }

            throw new AssertionError("Missing Adobe Analytics events: " + missingEvents);
        }

        System.out.println("✅ All expected Adobe Analytics events were found: " + expectedEvents);
    }


    public static void generateChart(String chartPath) {
                 DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            for (Map.Entry<Long, Integer> entry : apiCallsPerSecond.entrySet()) {
                String timeLabel = Instant.ofEpochSecond(entry.getKey()).toString(); // readable time
                dataset.addValue(entry.getValue(), "API Calls", timeLabel);
            }

            JFreeChart lineChart = ChartFactory.createBarChart(
                    "API Calls Over Time",
                    "Timestamp",
                    "Number of API Calls",
                    dataset
            );

            ChartPanel chartPanel = new ChartPanel(lineChart);
            chartPanel.setPreferredSize(new java.awt.Dimension(20, 10));
            try {
                File chartFile = new File("api_calls_chart.jpeg");
                ChartUtils.saveChartAsJPEG(new File(chartFile.getAbsolutePath()), lineChart, 1000, 600);
                System.out.println("✅ Chart saved as: " + chartFile);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("❌ Failed to save chart as JPEG");
            }

        }}



