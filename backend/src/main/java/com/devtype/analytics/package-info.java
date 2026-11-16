@org.springframework.modulith.ApplicationModule(
    displayName = "Analytics Module",
    allowedDependencies = {"training::api", "content::api", "identity::api", "gamification::api", "shared"}
)
package com.devtype.analytics;
