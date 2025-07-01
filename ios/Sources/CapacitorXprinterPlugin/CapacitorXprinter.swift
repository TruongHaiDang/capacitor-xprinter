import Foundation

@objc public class CapacitorXprinter: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
