//
//  POSIPConfigVC.h
//  Printer
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class PrinterProfile;
@interface POSIPConfigVC : UIViewController

+ (instancetype)initControllerWith:(PrinterProfile *)printer;

@end

NS_ASSUME_NONNULL_END
