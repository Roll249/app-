package com.fintech.data.remote.market

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Market Manager - Market Data Management
 * 
 * This manager provides access to all market data services.
 */
@Singleton
class MarketManager @Inject constructor(
    private val marketApi: MarketApi
) {
    
    /**
     * Get all market data
     */
    suspend fun getAllMarketData(): Result<MarketData> {
        return try {
            val response = marketApi.getAllMarketData()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get market data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get exchange rates
     */
    suspend fun getExchangeRates(): Result<List<ExchangeRate>> {
        return try {
            val response = marketApi.getExchangeRates()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get exchange rates"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get gold prices
     */
    suspend fun getGoldPrices(): Result<List<GoldPrice>> {
        return try {
            val response = marketApi.getGoldPrices()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get gold prices"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get stock indices
     */
    suspend fun getStockIndices(): Result<List<StockIndex>> {
        return try {
            val response = marketApi.getStockIndices()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get stock indices"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get crypto prices
     */
    suspend fun getCryptoPrices(): Result<List<CryptoPrice>> {
        return try {
            val response = marketApi.getCryptoPrices()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get crypto prices"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convert currency
     */
    suspend fun convertCurrency(
        amount: Double,
        from: String,
        to: String
    ): Result<CurrencyConversion> {
        return try {
            val response = marketApi.convertCurrency(amount, from, to)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to convert currency"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
