@org.springframework.modulith.ApplicationModule(
    displayName = "Coach Module",
    allowedDependencies = {"analytics::api", "training::api", "gamification::api", "content::api", "identity::api"}
)
package com.devtype.coach;
