package com.nutron.sampledragger.presentation.food

import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import com.nutron.sampledragger.base.RED_LEVEL
import com.nutron.sampledragger.base.YELLOW_LEVEL
import com.nutron.sampledragger.data.entity.Food
import com.nutron.sampledragger.data.network.UsdaApi
import com.nutron.sampledragger.extensions.addTo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


interface FoodInput {
    fun getFood(foodId: String)
    fun cleanup()
}

interface FoodOutput {
    val yellowReport: Observable<Food>
    val redReport: Observable<Food>
    val greenReport: Observable<Food>
    val unknownReport: Observable<Food>
    val showProgress: Observable<Boolean>
    val error: Observable<Throwable>
}

interface FoodViewModel {
    val input: FoodInput
    val output: FoodOutput
}

class FoodViewModelImpl(val api: UsdaApi): FoodViewModel, FoodInput, FoodOutput {

    override val input: FoodInput by RxReadOnlyProperty<FoodViewModelImpl, FoodInput>()
    override val output: FoodOutput by RxReadOnlyProperty<FoodViewModelImpl, FoodOutput>()
    override val yellowReport: BehaviorRelay<Food> = BehaviorRelay.create()
    override val redReport: BehaviorRelay<Food> = BehaviorRelay.create()
    override val greenReport: BehaviorRelay<Food> = BehaviorRelay.create()
    override val unknownReport: BehaviorRelay<Food> = BehaviorRelay.create()
    override val showProgress: BehaviorRelay<Boolean> = BehaviorRelay.create()
    override val error: BehaviorRelay<Throwable> = BehaviorRelay.create()

    val disposeBag = CompositeDisposable()

    override fun getFood(foodId: String) {
        api.getFoodItem(foodId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showProgress.accept(true) }
                .doOnComplete { showProgress.accept(false) }
                .map { it.foodList.foods[0] }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ food ->
                    handleOutput(food)
                }, { e ->
                    showProgress.accept(false)
                    error.accept(e)
                }).addTo(disposeBag)
    }

    override fun cleanup() {
        disposeBag.clear()
    }

    private fun handleOutput(food: Food) {
        val nutrients = food.nutrients
        nutrients?.let {
            val nutrient = it[0]
            try {
                val nutrientValue = nutrient.value.toDouble()
                if (nutrientValue < 0) {
                    unknownReport.accept(food)
                } else if (nutrientValue < YELLOW_LEVEL) {
                    greenReport.accept(food)
                } else if (nutrientValue < RED_LEVEL) {
                    yellowReport.accept(food)
                } else {
                    redReport.accept(food)
                }
            } catch (e: NumberFormatException) {
                Log.e("ResourceManager", "Error parsing nutrient value")
            }
        } ?: unknownReport.accept(food)
    }
}

private class RxReadOnlyProperty<in R, out T> : ReadOnlyProperty<R, T> {
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: R, property: KProperty<*>): T {
        return thisRef as T
    }
}