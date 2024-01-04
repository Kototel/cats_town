val screens = ":modules:screens"
val utils = ":modules:utils"

include(":apps:android")

// region Screens
include("$screens:main_screen")
// endregion

// region Utils
include("$utils:custom_view_tools")
// endregion