@org.springframework.modulith.ApplicationModule(
    displayName = "Admin Module",
    allowedDependencies = {"identity::api", "content::api", "training::api", "analytics::api", "gamification::api", "coach::api"}
)
package com.devtype.admin;
