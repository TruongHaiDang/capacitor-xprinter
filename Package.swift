// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorXprinter",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CapacitorXprinter",
            targets: ["CapacitorXprinterPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "CapacitorXprinterPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/CapacitorXprinterPlugin"),
        .testTarget(
            name: "CapacitorXprinterPluginTests",
            dependencies: ["CapacitorXprinterPlugin"],
            path: "ios/Tests/CapacitorXprinterPluginTests")
    ]
)