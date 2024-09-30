package com.bucheon.yeoddadae;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBinderMapperImpl extends DataBinderMapper {
  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(0);

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(2);
    result.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
    result.add(new com.tmapmobility.tmap.tmapsdk.ui.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(60);

    static {
      sKeys.put(0, "_all");
      sKeys.put(1, "appInitComplete");
      sKeys.put(2, "arrivalTimeMode");
      sKeys.put(3, "body");
      sKeys.put(4, "bottomAddressMode");
      sKeys.put(5, "buttonClickListener");
      sKeys.put(6, "callback");
      sKeys.put(7, "centerFeeVisible");
      sKeys.put(8, "centerVisible");
      sKeys.put(9, "compassVisible");
      sKeys.put(10, "complexCrossroadMinimized");
      sKeys.put(11, "complexCrossroadVisible");
      sKeys.put(12, "count");
      sKeys.put(13, "countType");
      sKeys.put(14, "currentPositionVisible");
      sKeys.put(15, "departName");
      sKeys.put(16, "destName");
      sKeys.put(17, "drivingData");
      sKeys.put(18, "drivingMode");
      sKeys.put(19, "firstDistance");
      sKeys.put(20, "firstDistanceUnit");
      sKeys.put(21, "firstGuide");
      sKeys.put(22, "firstVisible");
      sKeys.put(23, "fuelButtonClickable");
      sKeys.put(24, "fuelButtonSelected");
      sKeys.put(25, "fuelButtonVisible");
      sKeys.put(26, "headerItemSize");
      sKeys.put(27, "isAdViewVisible");
      sKeys.put(28, "isHighwayMiniMode");
      sKeys.put(29, "isNightMode");
      sKeys.put(30, "isOnHighway");
      sKeys.put(31, "laneViewOverlapped");
      sKeys.put(32, "leftButtonTitle");
      sKeys.put(33, "leftPocketExist");
      sKeys.put(34, "mapButtonMarginBottom");
      sKeys.put(35, "mapLayoutVisible");
      sKeys.put(36, "naviCurrentPositionVisible");
      sKeys.put(37, "naviViewVisible");
      sKeys.put(38, "naviZoomVisible");
      sKeys.put(39, "navigationVisible");
      sKeys.put(40, "nearViaPoint");
      sKeys.put(41, "orientation");
      sKeys.put(42, "previewBtnVisible");
      sKeys.put(43, "previewHeaderVisible");
      sKeys.put(44, "rightButtonTitle");
      sKeys.put(45, "rightPocketExist");
      sKeys.put(46, "rotationAngle");
      sKeys.put(47, "routeOption");
      sKeys.put(48, "routeSummaryInfo");
      sKeys.put(49, "secondDistance");
      sKeys.put(50, "secondDistanceUnit");
      sKeys.put(51, "secondVisible");
      sKeys.put(52, "simulationRepeatOnce");
      sKeys.put(53, "summaryViewVisible");
      sKeys.put(54, "tbtOrientation");
      sKeys.put(55, "tiltAngle");
      sKeys.put(56, "tollFee");
      sKeys.put(57, "uiMode");
      sKeys.put(58, "viaDataSize");
      sKeys.put(59, "viewMode");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(0);
  }
}
