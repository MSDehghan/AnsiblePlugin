plugins {
    java
}

repositories{
    mavenCentral()
}

dependencies {
    implementation(libs.jakson)
}


/* Manual changes:
1- Remove import_playbook from modules list and it's file.
2- Change type of 'mode' field in copy module to raw.
3- Remove "free-form" from module fields in thier files.
 */