package com.fintech.data.remote.market

import com.fintech.data.remote.model.ApiResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Market API - Financial Market Data
 * 
 * This interface provides endpoints to fetch:
 * - Exchange rates
 * - Gold prices
 * - Stock indices
 * - Cryptocurrency prices
 */
interface MarketApi {

    @GET("market")
    suspend fun getAllMarketData(): Response<ApiResponse<MarketData>>

    @GET("market/exchange-rates")
    suspend fun getExchangeRates(): Response<ApiResponse<List<ExchangeRate>>>

    @GET("market/gold-prices")
    suspend fun getGoldPrices(): Response<ApiResponse<List<GoldPrice>>>

    @GET("market/stock-indices")
    suspend fun getStockIndices(): Response<ApiResponse<List<StockIndex>>>

    @GET("market/crypto-prices")
    suspend fun getCryptoPrices(): Response<ApiResponse<List<CryptoPrice>>>

    @GET("market/convert")
    suspend fun convertCurrency(
        @Query("amount") amount: Double,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<ApiResponse<CurrencyConversion>>
}
