package com.fintech.data.remote.market

/**
 * Market Data Models
 * 
 * These models represent financial market data:
 * - Exchange rates
 * - Gold prices
 * - Stock indices
 * - Cryptocurrency prices
 */
data class ExchangeRate(
    val currency: String,
    val currencyName: String,
    val buyRate: Double,
    val sellRate: Double,
    val transferRate: Double,
    val lastUpdated: String
)

data class GoldPrice(
    val type: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val lastUpdated: String
)

data class StockIndex(
    val name: String,
    val code: String,
    val currentValue: Double,
    val change: Double,
    val changePercent: Double,
    val lastUpdated: String
)

data class CryptoPrice(
    val symbol: String,
    val name: String,
    val price: Double,
    val change24h: Double,
    val changePercent24h: Double,
    val lastUpdated: String
)

data class MarketData(
    val exchangeRates: List<ExchangeRate>,
    val goldPrices: List<GoldPrice>,
    val stockIndices: List<StockIndex>,
    val cryptoPrices: List<CryptoPrice>
)

data class CurrencyConversion(
    val amount: Double,
    val from: String,
    val to: String,
    val result: Double
)
