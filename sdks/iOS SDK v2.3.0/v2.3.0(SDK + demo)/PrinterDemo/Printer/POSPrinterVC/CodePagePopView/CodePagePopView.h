//
//  CodePagePopView.h
//  Printer
//
//  Created by Apple Mac mini intel on 2022/11/24.
//  Copyright © 2022 Admin. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class CodePagePopView;
@protocol CodePageViewDelegate <NSObject>
- (void)codepageView:(CodePagePopView *)codepagePopView selectValue:(NSString *)selectValue;
@end

@interface CodePagePopView : UIView

@property (weak, nonatomic) id<CodePageViewDelegate> delegate;

+ (CodePagePopView *)initViewWithArray:(NSArray *)array;

- (void)hideView;

@end

NS_ASSUME_NONNULL_END
