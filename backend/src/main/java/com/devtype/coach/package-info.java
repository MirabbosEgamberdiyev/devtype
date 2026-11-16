@org.springframework.modulith.ApplicationModule(
    displayName = "Coach Module",
    allowedDependencies = {"analytics::api", "training::api", "gamification::api", "content::api", "identity::api", "shared"}
)
package com.devtype.coach;
