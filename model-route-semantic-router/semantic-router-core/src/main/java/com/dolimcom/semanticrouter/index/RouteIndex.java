package com.dolimcom.semanticrouter.index;

import com.dolimcom.semanticrouter.model.RouteScore;
import com.dolimcom.semanticrouter.model.RouteSnapshot;

import java.util.List;

public interface RouteIndex {

    List<RouteScore> search(RouteSnapshot snapshot, String input, double[] inputEmbedding, int limit);
}
