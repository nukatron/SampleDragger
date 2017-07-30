package com.nutron.sampledragger.data.network

import com.nutron.sampledragger.BuildConfig
import com.nutron.sampledragger.base.SUGAR_NUTRIENT
import com.nutron.sampledragger.data.entity.FoodResponse
import com.nutron.sampledragger.data.entity.FoodzListResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface UsdaApi {

    @GET("ndb/list?")
    fun getFoodzList(@Query("api_key") key:String = BuildConfig.API_KEY): Observable<FoodzListResponse>

    @GET("ndb/nutrients?")
    fun getFoodItem(@Query("ndbno") foodId: String,
                    @Query("api_key") key:String = BuildConfig.API_KEY,
                    @Query("nutrients") nutrients :String = SUGAR_NUTRIENT): Observable<FoodResponse>

}
