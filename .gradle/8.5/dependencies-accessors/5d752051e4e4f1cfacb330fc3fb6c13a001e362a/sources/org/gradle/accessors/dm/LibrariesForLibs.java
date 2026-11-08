package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the `libs` extension.
 */
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

        /**
         * Creates a dependency provider for activity (androidx.activity:activity-ktx)
     * with versionRef 'activity'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getActivity() {
            return create("activity");
    }

        /**
         * Creates a dependency provider for agp (com.android.tools.build:gradle)
     * with versionRef 'agp'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAgp() {
            return create("agp");
    }

        /**
         * Creates a dependency provider for androidxTestCore (androidx.test:core)
     * with versionRef 'androidxTest'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAndroidxTestCore() {
            return create("androidxTestCore");
    }

        /**
         * Creates a dependency provider for androidxTestExt (androidx.test.ext:junit)
     * with versionRef 'androidxTest'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAndroidxTestExt() {
            return create("androidxTestExt");
    }

        /**
         * Creates a dependency provider for androidxTestRules (androidx.test:rules)
     * with versionRef 'androidxTestRules'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAndroidxTestRules() {
            return create("androidxTestRules");
    }

        /**
         * Creates a dependency provider for androidxTestRunner (androidx.test:runner)
     * with versionRef 'androidxTestRunner'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAndroidxTestRunner() {
            return create("androidxTestRunner");
    }

        /**
         * Creates a dependency provider for appcompat (androidx.appcompat:appcompat)
     * with versionRef 'appcompat'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAppcompat() {
            return create("appcompat");
    }

        /**
         * Creates a dependency provider for cameraxCamera2 (androidx.camera:camera-camera2)
     * with versionRef 'camerax'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCameraxCamera2() {
            return create("cameraxCamera2");
    }

        /**
         * Creates a dependency provider for cameraxCore (androidx.camera:camera-core)
     * with versionRef 'camerax'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCameraxCore() {
            return create("cameraxCore");
    }

        /**
         * Creates a dependency provider for cameraxLifecycle (androidx.camera:camera-lifecycle)
     * with versionRef 'camerax'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCameraxLifecycle() {
            return create("cameraxLifecycle");
    }

        /**
         * Creates a dependency provider for cameraxView (androidx.camera:camera-view)
     * with versionRef 'camerax'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCameraxView() {
            return create("cameraxView");
    }

        /**
         * Creates a dependency provider for coreKtx (androidx.core:core-ktx)
     * with versionRef 'coreKtx'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCoreKtx() {
            return create("coreKtx");
    }

        /**
         * Creates a dependency provider for coroutines (org.jetbrains.kotlinx:kotlinx-coroutines-android)
     * with versionRef 'coroutines'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCoroutines() {
            return create("coroutines");
    }

        /**
         * Creates a dependency provider for coroutinesCore (org.jetbrains.kotlinx:kotlinx-coroutines-core)
     * with versionRef 'coroutines'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCoroutinesCore() {
            return create("coroutinesCore");
    }

        /**
         * Creates a dependency provider for espressoCore (androidx.test.espresso:espresso-core)
     * with versionRef 'espressoCore'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getEspressoCore() {
            return create("espressoCore");
    }

        /**
         * Creates a dependency provider for junit (junit:junit)
     * with versionRef 'junit'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJunit() {
            return create("junit");
    }

        /**
         * Creates a dependency provider for junitJupiter (org.junit.jupiter:junit-jupiter)
     * with versionRef 'junitVersion'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJunitJupiter() {
            return create("junitJupiter");
    }

        /**
         * Creates a dependency provider for kotlinAndroid (org.jetbrains.kotlin:kotlin-android)
     * with versionRef 'kotlin'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getKotlinAndroid() {
            return create("kotlinAndroid");
    }

        /**
         * Creates a dependency provider for kotlinJvm (org.jetbrains.kotlin:kotlin-stdlib-jdk8)
     * with versionRef 'kotlin'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getKotlinJvm() {
            return create("kotlinJvm");
    }

        /**
         * Creates a dependency provider for kotlinSerialization (org.jetbrains.kotlin:kotlinx-serialization-json)
     * with versionRef 'serialization'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getKotlinSerialization() {
            return create("kotlinSerialization");
    }

        /**
         * Creates a dependency provider for ksp (com.google.devtools.ksp:ksp)
     * with versionRef 'ksp'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getKsp() {
            return create("ksp");
    }

        /**
         * Creates a dependency provider for lifecycleRuntime (androidx.lifecycle:lifecycle-runtime-ktx)
     * with versionRef 'lifecycle'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getLifecycleRuntime() {
            return create("lifecycleRuntime");
    }

        /**
         * Creates a dependency provider for lifecycleViewModel (androidx.lifecycle:lifecycle-viewmodel-ktx)
     * with versionRef 'lifecycle'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getLifecycleViewModel() {
            return create("lifecycleViewModel");
    }

        /**
         * Creates a dependency provider for material (com.google.android.material:material)
     * with versionRef 'material'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getMaterial() {
            return create("material");
    }

        /**
         * Creates a dependency provider for mlkitBarcode (com.google.mlkit:barcode-scanning)
     * with versionRef 'mlkitBarcode'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getMlkitBarcode() {
            return create("mlkitBarcode");
    }

        /**
         * Creates a dependency provider for okhttp (com.squareup.okhttp3:okhttp)
     * with versionRef 'okhttp'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getOkhttp() {
            return create("okhttp");
    }

        /**
         * Creates a dependency provider for okio (com.squareup.okio:okio)
     * with versionRef 'okhttp'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getOkio() {
            return create("okio");
    }

        /**
         * Creates a dependency provider for robolectric (org.robolectric:robolectric)
     * with versionRef 'robolectric'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getRobolectric() {
            return create("robolectric");
    }

        /**
         * Creates a dependency provider for roomCompiler (androidx.room:room-compiler)
     * with versionRef 'room'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getRoomCompiler() {
            return create("roomCompiler");
    }

        /**
         * Creates a dependency provider for roomKtx (androidx.room:room-ktx)
     * with versionRef 'room'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getRoomKtx() {
            return create("roomKtx");
    }

        /**
         * Creates a dependency provider for roomRuntime (androidx.room:room-runtime)
     * with versionRef 'room'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getRoomRuntime() {
            return create("roomRuntime");
    }

        /**
         * Creates a dependency provider for truth (com.google.truth:truth)
     * with versionRef 'truth'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getTruth() {
            return create("truth");
    }

        /**
         * Creates a dependency provider for usbSerial (com.github.mik3y:usb-serial-for-android)
     * with versionRef 'usbSerial'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getUsbSerial() {
            return create("usbSerial");
    }

        /**
         * Creates a dependency provider for websocket (org.java-websocket:Java-WebSocket)
     * with versionRef 'websocket'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getWebsocket() {
            return create("websocket");
    }

        /**
         * Creates a dependency provider for zxing (com.google.zxing:core)
     * with versionRef 'zxing'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getZxing() {
            return create("zxing");
    }

    /**
     * Returns the group of versions at versions
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Returns the group of bundles at bundles
     */
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    /**
     * Returns the group of plugins at plugins
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class VersionAccessors extends VersionFactory  {

        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: activity (1.8.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getActivity() { return getVersion("activity"); }

            /**
             * Returns the version associated to this alias: agp (8.3.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getAgp() { return getVersion("agp"); }

            /**
             * Returns the version associated to this alias: androidxTest (1.5.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getAndroidxTest() { return getVersion("androidxTest"); }

            /**
             * Returns the version associated to this alias: androidxTestRules (1.5.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getAndroidxTestRules() { return getVersion("androidxTestRules"); }

            /**
             * Returns the version associated to this alias: androidxTestRunner (1.5.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getAndroidxTestRunner() { return getVersion("androidxTestRunner"); }

            /**
             * Returns the version associated to this alias: appcompat (1.6.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getAppcompat() { return getVersion("appcompat"); }

            /**
             * Returns the version associated to this alias: camerax (1.3.4)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getCamerax() { return getVersion("camerax"); }

            /**
             * Returns the version associated to this alias: coreKtx (1.12.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getCoreKtx() { return getVersion("coreKtx"); }

            /**
             * Returns the version associated to this alias: coroutines (1.7.3)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getCoroutines() { return getVersion("coroutines"); }

            /**
             * Returns the version associated to this alias: espressoCore (3.5.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getEspressoCore() { return getVersion("espressoCore"); }

            /**
             * Returns the version associated to this alias: junit (4.13.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getJunit() { return getVersion("junit"); }

            /**
             * Returns the version associated to this alias: junitVersion (1.1.5)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getJunitVersion() { return getVersion("junitVersion"); }

            /**
             * Returns the version associated to this alias: kotlin (1.9.20)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getKotlin() { return getVersion("kotlin"); }

            /**
             * Returns the version associated to this alias: ksp (1.9.20-1.0.14)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getKsp() { return getVersion("ksp"); }

            /**
             * Returns the version associated to this alias: lifecycle (2.7.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getLifecycle() { return getVersion("lifecycle"); }

            /**
             * Returns the version associated to this alias: material (1.10.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getMaterial() { return getVersion("material"); }

            /**
             * Returns the version associated to this alias: mlkitBarcode (17.2.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getMlkitBarcode() { return getVersion("mlkitBarcode"); }

            /**
             * Returns the version associated to this alias: okhttp (4.12.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getOkhttp() { return getVersion("okhttp"); }

            /**
             * Returns the version associated to this alias: robolectric (4.11)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getRobolectric() { return getVersion("robolectric"); }

            /**
             * Returns the version associated to this alias: room (2.6.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getRoom() { return getVersion("room"); }

            /**
             * Returns the version associated to this alias: serialization (1.6.3)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getSerialization() { return getVersion("serialization"); }

            /**
             * Returns the version associated to this alias: truth (1.1.3)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getTruth() { return getVersion("truth"); }

            /**
             * Returns the version associated to this alias: usbSerial (3.8.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getUsbSerial() { return getVersion("usbSerial"); }

            /**
             * Returns the version associated to this alias: websocket (1.5.7)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getWebsocket() { return getVersion("websocket"); }

            /**
             * Returns the version associated to this alias: zxing (3.5.3)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getZxing() { return getVersion("zxing"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

    }

    public static class PluginAccessors extends PluginFactory {

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a plugin provider for androidApplication to the plugin id 'com.android.application'
             * with versionRef 'agp'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getAndroidApplication() { return createPlugin("androidApplication"); }

            /**
             * Creates a plugin provider for kotlinAndroid to the plugin id 'org.jetbrains.kotlin.android'
             * with versionRef 'kotlin'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getKotlinAndroid() { return createPlugin("kotlinAndroid"); }

            /**
             * Creates a plugin provider for kotlinJvm to the plugin id 'org.jetbrains.kotlin.jvm'
             * with versionRef 'kotlin'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getKotlinJvm() { return createPlugin("kotlinJvm"); }

            /**
             * Creates a plugin provider for kotlinSerialization to the plugin id 'org.jetbrains.kotlin.plugin.serialization'
             * with versionRef 'kotlin'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getKotlinSerialization() { return createPlugin("kotlinSerialization"); }

    }

}
