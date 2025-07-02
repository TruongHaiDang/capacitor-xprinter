//
//  DIPViewController.m
//  Printer
//

#import "DIPSettingVC.h"
#import "POSWIFIManager.h"
#import "POSCommand.h"

@interface DIPSettingVC ()

@property (weak, nonatomic) IBOutlet UISegmentedControl *selectCutter;
@property (weak, nonatomic) IBOutlet UISegmentedControl *selectBeeper;
@property (weak, nonatomic) IBOutlet UISegmentedControl *doubleCharacterMode;
@property (weak, nonatomic) IBOutlet UISegmentedControl *characterPerLineFont;
@property (weak, nonatomic) IBOutlet UISegmentedControl *cutterWithDrawer;
@property (weak, nonatomic) IBOutlet UISegmentedControl *serialBaudrate;

@end

@implementation DIPSettingVC

- (void)viewDidLoad {
    [super viewDidLoad];
}

- (IBAction)settingAction:(UIButton *)sender {
    NSString *cutterStr = @"";
    if (self.selectCutter.selectedSegmentIndex == 0) {
        cutterStr = @"0";
    } else {
        cutterStr = @"1";
    }
    
    NSString *beeperStr = @"";
    if (self.selectBeeper.selectedSegmentIndex == 0) {
        beeperStr = @"1";
    } else {
        beeperStr = @"0";
    }
    
    NSString *doubleCharacterModeStr = @"";
    if (self.doubleCharacterMode.selectedSegmentIndex == 0) {
        doubleCharacterModeStr = @"0";
    } else {
        doubleCharacterModeStr = @"1";
    }
    
    NSString *characterPerLineFontStr = @"";
    if (self.characterPerLineFont.selectedSegmentIndex == 0) {
        characterPerLineFontStr = @"0";
    } else {
        characterPerLineFontStr = @"1";
    }
    
    NSString *cutterWithDrawerStr = @"";
    if (self.cutterWithDrawer.selectedSegmentIndex == 0) {
        cutterWithDrawerStr = @"1";
    } else {
        cutterWithDrawerStr = @"0";
    }
    
    NSString *serialBaudrateStr0 = @"";
    NSString *serialBaudrateStr1 = @"";
    switch (self.serialBaudrate.selectedSegmentIndex) {
        case 0:
        {
            serialBaudrateStr0 = @"1";
            serialBaudrateStr1 = @"0";
        }
            break;
            
        case 1:
        {
            serialBaudrateStr0 = @"0";
            serialBaudrateStr1 = @"0";
        }
            break;
            
        case 2:
        {
            serialBaudrateStr0 = @"1";
            serialBaudrateStr1 = @"1";
        }
            break;
            
        case 3:
        {
            serialBaudrateStr0 = @"0";
            serialBaudrateStr1 = @"1";
        }
            break;
            
        default:
            break;
    }
    
    NSString *hexStr = [NSString stringWithFormat:@"%@%@%@%@%@0%@%@", serialBaudrateStr1, serialBaudrateStr0, cutterWithDrawerStr, characterPerLineFontStr, doubleCharacterModeStr, beeperStr, cutterStr];
    [[POSWIFIManager sharedInstance] writeCommandWithData:[POSCommand setDIPSettingsWithString:hexStr]];
    [self.navigationController popToRootViewControllerAnimated:YES];
}

@end
