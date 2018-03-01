package com.unity3d.ads.placement;

import com.unity3d.ads.UnityAds.PlacementState;
import java.util.HashMap;

public class Placement {
    private static String _defaultPlacement;
    private static HashMap<String, PlacementState> _placementReadyMap;

    public static void setDefaultPlacement(String placement) {
        _defaultPlacement = placement;
    }

    public static void setPlacementState(String placement, String placementState) {
        if (_placementReadyMap == null) {
            _placementReadyMap = new HashMap();
        }
        _placementReadyMap.put(placement, PlacementState.valueOf(placementState));
    }

    public static boolean isReady(String placement) {
        return getPlacementState(placement) == PlacementState.READY;
    }

    public static boolean isReady() {
        return getPlacementState() == PlacementState.READY;
    }

    public static PlacementState getPlacementState(String placement) {
        return currentState(placement);
    }

    public static PlacementState getPlacementState() {
        if (_defaultPlacement == null) {
            return PlacementState.NOT_AVAILABLE;
        }
        return getPlacementState(_defaultPlacement);
    }

    public static void reset() {
        _placementReadyMap = null;
        _defaultPlacement = null;
    }

    public static String getDefaultPlacement() {
        return _defaultPlacement;
    }

    private static PlacementState currentState(String placement) {
        if (_placementReadyMap == null || !_placementReadyMap.containsKey(placement)) {
            return PlacementState.NOT_AVAILABLE;
        }
        return (PlacementState) _placementReadyMap.get(placement);
    }
}
