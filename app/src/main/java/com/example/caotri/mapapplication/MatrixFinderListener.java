package com.example.caotri.mapapplication;

import java.util.List;

/**
 * Created by Cao Tri on 23-Jun-16.
 */
public interface MatrixFinderListener {
    void onMatrixFinderStart();
    void onMatrixFinderSuccess(List<Route> routes, int i);

}
