package com.modelroute.router;

import com.modelroute.config.ModelRouteProperties;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Extracts the actionable clause using configuration-owned discourse markers.
 */
@Component
public class RoutingTextFocusExtractor {

    private final ModelRouteProperties properties;

    public RoutingTextFocusExtractor(ModelRouteProperties properties) {
        this.properties = properties;
    }

    public FocusedRoutingText extract(String input) {
        String original = input == null ? "" : input.trim();
        ModelRouteProperties.FocusRouting focus = properties.getRouter().getFocus();
        if (original.isEmpty() || focus == null || !focus.isEnabled()) {
            return new FocusedRoutingText(original, false, false);
        }

        String focused = stripAcknowledgement(original, focus.getAcknowledgements());
        boolean contextSwitch = containsAny(focused, focus.getSwitchMarkers());

        int switchBoundary = contextSwitch
                ? boundaryAfterSwitch(focused, focus.getSwitchMarkers(), focus.getBoundaryDelimiters())
                : -1;
        if (switchBoundary >= 0) {
            focused = focused.substring(switchBoundary).trim();
        } else {
            contextSwitch = false;
        }

        int markerBoundary = boundaryAfterLastMarker(focused, focus.getFocusMarkers());
        if (markerBoundary >= 0) {
            focused = focused.substring(markerBoundary).trim();
        }

        int requestBoundary = boundaryAtLastRequestMarker(focused, focus.getRequestMarkers());
        if (requestBoundary > 0) {
            String requestedClause = focused.substring(requestBoundary).trim();
            if (!startsWithAny(requestedClause, focus.getIgnoredRequestPrefixes())
                    && requestedClause.length() >= focus.getMinimumFocusedLength()) {
                focused = requestedClause;
            }
        }

        focused = stripTrailingConstraint(focused, focus.getTrailingConstraints()).trim();
        if (focused.length() < focus.getMinimumFocusedLength()) {
            focused = original;
            contextSwitch = false;
        }
        return new FocusedRoutingText(focused, contextSwitch, !focused.equals(original));
    }

    private int boundaryAfterSwitch(String text, List<String> switchMarkers, List<String> delimiters) {
        int markerIndex = lastMarkerIndex(text, switchMarkers);
        if (markerIndex < 0) {
            return -1;
        }
        int boundary = firstDelimiterBoundaryAfter(text, markerIndex, delimiters);
        if (boundary < 0 || boundary >= text.length() || text.substring(boundary).isBlank()) {
            return -1;
        }
        return boundary;
    }

    private int boundaryAfterLastMarker(String text, List<String> markers) {
        int bestIndex = -1;
        int boundary = -1;
        for (String marker : safe(markers)) {
            int index = text.lastIndexOf(marker);
            if (index > bestIndex) {
                bestIndex = index;
                boundary = index + marker.length();
            }
        }
        return boundary;
    }

    private int boundaryAtLastRequestMarker(String text, List<String> requestMarkers) {
        return lastMarkerIndex(text, requestMarkers);
    }

    private int firstDelimiterBoundaryAfter(String text, int fromIndex, List<String> delimiters) {
        int firstIndex = -1;
        int boundary = -1;
        for (String delimiter : safe(delimiters)) {
            if (delimiter == null || delimiter.isEmpty()) {
                continue;
            }
            int index = text.indexOf(delimiter, fromIndex);
            if (index >= 0 && (firstIndex < 0 || index < firstIndex)) {
                firstIndex = index;
                boundary = index + delimiter.length();
            }
        }
        return boundary;
    }

    private String stripTrailingConstraint(String text, List<String> markers) {
        int boundary = text.length();
        for (String marker : safe(markers)) {
            int index = text.indexOf(marker);
            if (index >= 0 && index < boundary) {
                boundary = index;
            }
        }
        return text.substring(0, boundary);
    }

    private String stripAcknowledgement(String text, List<String> acknowledgements) {
        for (String acknowledgement : safe(acknowledgements)) {
            if (text.startsWith(acknowledgement)) {
                return text.substring(acknowledgement.length()).trim();
            }
        }
        return text;
    }

    private boolean containsAny(String text, List<String> markers) {
        return safe(markers).stream().filter(marker -> marker != null && !marker.isEmpty()).anyMatch(text::contains);
    }

    private boolean startsWithAny(String text, List<String> prefixes) {
        return safe(prefixes).stream().filter(prefix -> prefix != null && !prefix.isEmpty()).anyMatch(text::startsWith);
    }

    private int lastMarkerIndex(String text, List<String> markers) {
        return safe(markers).stream()
                .filter(marker -> marker != null && !marker.isEmpty())
                .mapToInt(text::lastIndexOf)
                .max()
                .orElse(-1);
    }

    private List<String> safe(List<String> values) {
        return values == null ? List.of() : values;
    }

    public record FocusedRoutingText(String text, boolean contextSwitch, boolean changed) {
    }
}
