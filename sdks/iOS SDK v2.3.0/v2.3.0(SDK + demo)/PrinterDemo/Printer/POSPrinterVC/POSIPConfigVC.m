//
//  POSIPConfigVC.m
//  Printer
//

#import "POSIPConfigVC.h"
#import "POSWIFIManager.h"
#import "UIView+Toast.h"

@interface POSIPConfigVC ()<UITextFieldDelegate, POSWIFIManagerDelegate>

@property (weak, nonatomic) IBOutlet UITextField *ipTextField;
@property (weak, nonatomic) IBOutlet UITextField *maskTextField;
@property (weak, nonatomic) IBOutlet UITextField *gatewayTextField;
@property (weak, nonatomic) IBOutlet UISwitch *dhcpSwitch;
@property (weak, nonatomic) IBOutlet UIButton *applySettingsButton;
@property (weak, nonatomic) IBOutlet UIButton *connectPrinterButton;

@property (strong, nonatomic) PrinterProfile *printer;

@end

@implementation POSIPConfigVC

+ (instancetype)initControllerWith:(PrinterProfile *)printer {
    return [[self alloc] initWithPrinter:printer];
}

- (instancetype)initWithPrinter:(PrinterProfile *)printer {
    self = [super init];
    
    if (self) {
        UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:[NSBundle mainBundle]];
        self = (POSIPConfigVC *)[sb instantiateViewControllerWithIdentifier:@"POSIPConfigVC"];
        self.printer = printer;
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self setupView];
    
    [POSWIFIManager sharedInstance].delegate = self;
}

- (void)dealloc {
    [[POSWIFIManager sharedInstance] removeDelegate:self];
}

- (void)setupView {
    UILabel *iplabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 100, _ipTextField.frame.size.height)];
    iplabel.text = @" IP:";
    _ipTextField.text = [self.printer getIPString];
    _ipTextField.leftView = iplabel;
    _ipTextField.leftViewMode = UITextFieldViewModeAlways;
    _ipTextField.delegate = self;
    
    UILabel *masklabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 100, _ipTextField.frame.size.height)];
    masklabel.text = @" MASK:";
    _maskTextField.text = [self.printer getMaskString];
    _maskTextField.leftView = masklabel;
    _maskTextField.leftViewMode = UITextFieldViewModeAlways;
    _maskTextField.delegate = self;
    
    UILabel *gwlabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 100, _ipTextField.frame.size.height)];
    gwlabel.text = @" GATEWAY:";
    _gatewayTextField.text = [self.printer getGatewayString];
    _gatewayTextField.leftView = gwlabel;
    _gatewayTextField.leftViewMode = UITextFieldViewModeAlways;
    _gatewayTextField.delegate = self;
    
    _dhcpSwitch.on = [self.printer getDHCP];
    
    _applySettingsButton.layer.borderWidth = 1.f;
    _applySettingsButton.layer.cornerRadius = 10.f;
    _applySettingsButton.layer.borderColor = [UIColor systemBlueColor].CGColor;
    
    _connectPrinterButton.layer.borderWidth = 1.f;
    _connectPrinterButton.layer.cornerRadius = 10.f;
    _connectPrinterButton.layer.borderColor = [UIColor systemBlueColor].CGColor;
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(keyboardDismiss)];
    self.view.userInteractionEnabled = YES;
    [self.view addGestureRecognizer:tap];
}

- (void)keyboardDismiss {
    if (_ipTextField.editing)
        [_ipTextField resignFirstResponder];
    
    if (_maskTextField.editing)
        [_maskTextField resignFirstResponder];
    
    if (_gatewayTextField.editing)
        [_gatewayTextField resignFirstResponder];
}

- (IBAction)applySettingsAction:(UIButton *)sender {
    BOOL dhcp = self.dhcpSwitch.on ? YES : NO;
    [[POSWIFIManager sharedInstance] setIPConfigWithIP:self.ipTextField.text Mask:self.maskTextField.text Gateway:self.gatewayTextField.text DHCP:dhcp];
    [self keyboardDismiss];
    
    [self popController];
}

- (IBAction)connectPrinterAction:(UIButton *)sender {
    // connet printer with mac address
    [[POSWIFIManager sharedInstance] connectWithMac:self.printer.printerName];
}

- (void)popController {
    NSArray *viewControllers = self.navigationController.viewControllers;
    [self.navigationController popToViewController:viewControllers[1] animated:YES];
}


#pragma mark - UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}


#pragma mark - POSWIFIManagerDelegate

- (void)POSwifiConnectedToHost:(NSString *)host port:(UInt16)port {
    [self popController];
}

- (void)POSwifiDisconnectWithError:(NSError *)error {
    if (error) {
        [self.view makeToast:[NSString stringWithFormat:@"%@", error] duration:2.f position:CSToastPositionCenter];
    }
}

@end
