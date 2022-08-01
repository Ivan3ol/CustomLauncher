# CustomLauncher

Custom Launcher (Home screen) with time clock, battery indicator, apps drawer and custom widget.

## Classes breakdown

### BaseActivity/BaseFragment/BaseViewModel

Fundament of UI and ViewModel architecture, all Fragments, Activities and ViewModels should extend these abstract classes. In the current implementation these classes do not contain any particular functionality, except setting up.

### AppLaunchInfo

Data class for app drawer recycler view. Contains information about the app to show it in the recycler view and launch on click.

### WeatherInfo

Data class for custom widget. Contains infromation about current weather in particular location. Also contains some error values.

### WeatherAPI

Class responsible for comunication with external 3-rd party RESTful API. Contains inner Response class, which is a wrapper for a query result. 

### WeatherRepository

Repository used to communicate with WeatherAPI class.

### ChargeReceiver

Broadcast reciver used to listen for changes of the charge level. Used for battery indicator in HomeFragment

### TimeReceiver

Broadcast reciver used to track time. Used for clock in HomeFragment

### GridWidgetService

Service that returns Remote Views factory instance. Used by widget provider to link factory with widget.

### MainActivity

Main and the only one activity in the app. Responsible for transitions between Home and AppDrawer fragments.

### AppDrawerFragment

Fragment with list of all apps displayed in Recycler View.

### AppDrawerViewModel

ViewModel of AppDrawerFragment. Responisble for fetching information about installed apps and passing it to the recycler view adapter.

### AppDrawerAdapter

Adapter of the app list recycler view.

### HomeFragment

Fragment with battery indicator, clock and custom widget.

### HomeViewModel

ViewModel of HomeFragment. Contains LiveData for current time and charge level.

### WeatherWidget

Widget provider class. Loads weather data using WeatherRepository and saves it to the content provider whenever update is needed. 

### WeatherContentObserver 

Observer, which listens for changes in content provider's data. Notifies remote views adapter (and respectively remote views factory) upon change.

### WeatherContentProvider 

Content provider which saves and updates current available weather data and returns it upon query.

### GridRemoteViewsFactory

Class that creates remote views for saved weather data. When notified, loads new data from content provider.
