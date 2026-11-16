@org.springframework.modulith.ApplicationModule(
    displayName = "Gamification Module",
    allowedDependencies = {"training::api", "analytics::api", "content::api", "identity::api"}
)
package com.devtype.gamification;
