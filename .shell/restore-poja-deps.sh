#!/bin/bash
set -e

# Restore spring-boot-starter-validation
if ! grep -q 'spring-boot-starter-validation' build.gradle; then
    sed -i '/spring-boot-starter-data-jpa/a\    implementation '\''org.springframework.boot:spring-boot-starter-validation:3.2.2'\'' ' build.gradle
fi

# Restore integration test exclusion
if ! grep -q 'excludeTags' build.gradle; then
    sed -i 's/useJUnitPlatform()/useJUnitPlatform {\n        excludeTags '\''integration'\''\n    }/' build.gradle
    cat >> build.gradle <<'EOF'

task integrationTest(type: Test) {
    useJUnitPlatform {
        includeTags 'integration'
    }
    shouldRunAfter test
}
EOF
fi
