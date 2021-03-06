package dev.mtoto.forecast

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import com.jakewharton.threetenabp.AndroidThreeTen
import dev.mtoto.forecast.data.db.ForecastDatabase
import dev.mtoto.forecast.data.network.*
import dev.mtoto.forecast.data.provider.LocationProvider
import dev.mtoto.forecast.data.provider.LocationProviderImpl
import dev.mtoto.forecast.data.provider.UnitProvider
import dev.mtoto.forecast.data.provider.UnitProviderImpl
import dev.mtoto.forecast.data.repository.ForecastRepository
import dev.mtoto.forecast.data.repository.ForecastRepositoryImpl
import dev.mtoto.forecast.ui.weather.current.CurrentWeatherViewModelFactory
import dev.mtoto.forecast.ui.weather.future.futureweatherdetail.FutureWeatherDetailViewModelFactory
import dev.mtoto.forecast.ui.weather.future.list.FutureWeatherListViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.*
import org.threeten.bp.LocalDate

class ForecastApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@ForecastApplication))

        bind() from singleton { ForecastDatabase(instance()) }
        bind() from singleton { instance<ForecastDatabase>().currentWeatherDao() }
        bind() from singleton { instance<ForecastDatabase>().WeatherLocationDao() }
        bind() from singleton { instance<ForecastDatabase>().futureWeatherDao() }
        bind<ConnectivityInterceptor>() with singleton { ConnectivityInterceptorImpl(instance()) }
        bind() from singleton { ApixuWeatherApiService(instance()) }
        bind() from provider { LocationServices.getFusedLocationProviderClient(instance<Context>()) }
        bind<WeatherNetworkDataSource>() with singleton { WeatherNetworkDataSourceImpl(instance()) }
        bind<LocationProvider>() with singleton { LocationProviderImpl(instance(), instance()) }

        bind<ForecastRepository>() with singleton {
            ForecastRepositoryImpl(
                instance(),
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
        bind<UnitProvider>() with singleton { UnitProviderImpl(instance()) }
        bind() from provider { CurrentWeatherViewModelFactory(instance(), instance()) }
        bind() from provider { FutureWeatherListViewModelFactory(instance(), instance()) }

        bind() from factory { detailedDate: LocalDate ->
            FutureWeatherDetailViewModelFactory(
                detailedDate,
                instance(),
                instance()
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }
}