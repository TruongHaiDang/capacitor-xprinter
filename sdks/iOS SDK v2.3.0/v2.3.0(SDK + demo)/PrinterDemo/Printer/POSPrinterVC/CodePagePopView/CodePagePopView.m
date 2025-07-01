//
//  CodePagePopView.m
//  Printer
//
//  Created by Apple Mac mini intel on 2022/11/24.
//  Copyright © 2022 Admin. All rights reserved.
//

#import "CodePagePopView.h"

@interface CodePagePopView()<UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) UITableView *tableView;
@property (nonatomic, strong) NSArray *codepageArray;
@property (nonatomic, strong) UIView *backgroundView;

@end

@implementation CodePagePopView

+ (CodePagePopView *)initViewWithArray:(NSArray *)array {
    return [[self alloc] initViewWithArray:array];
}

- (instancetype)initViewWithArray:(NSArray *)array {
    self = [super init];
    
    if (self) {
        UIWindow *window = (UIWindow*)[UIApplication sharedApplication].keyWindow;
        
        _backgroundView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, [UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.height)];
        _backgroundView.backgroundColor = [UIColor blackColor];
        _backgroundView.alpha = 0.5;
        _backgroundView.userInteractionEnabled = YES;
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideView)];
        [_backgroundView addGestureRecognizer:tap];
        [window addSubview:_backgroundView];
        
        CGFloat height = array.count * 40;
        if (height > 400) {
            height = 400;
        }
        self.frame = CGRectMake(0, 200, [UIScreen mainScreen].bounds.size.width, height);
        [window addSubview:self];
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, self.bounds.size.width, height)];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        [self addSubview:_tableView];
        
        _codepageArray = [NSArray arrayWithArray:array];
        [_tableView reloadData];
    }
    
    return self;
}

- (void)hideView {
    [_backgroundView removeFromSuperview];
    [self removeFromSuperview];
}

- (nonnull UITableViewCell *)tableView:(nonnull UITableView *)tableView cellForRowAtIndexPath:(nonnull NSIndexPath *)indexPath {
    static NSString *cellId = @"codepageCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellId];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellId];
    }
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    cell.textLabel.text = self.codepageArray[indexPath.row];
    return cell;
}

- (NSInteger)tableView:(nonnull UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.codepageArray.count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 40;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if (self.delegate) {
        if ([self.delegate respondsToSelector:@selector(codepageView:selectValue:)]) {
            NSString *value = self.codepageArray[indexPath.row];
            [self.delegate codepageView:self selectValue:value];
        }
    }
}

@end
