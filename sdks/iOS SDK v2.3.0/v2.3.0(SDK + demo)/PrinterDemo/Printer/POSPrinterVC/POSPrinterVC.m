#import "POSPrinterVC.h"
#import "POSPrinterSDK.h"
#import "CodePagePopView.h"
#import "UIView+Toast.h"
#import "PTable.h"

#define Language @"Language"
#define Cancel @"Cancel"
#define Simplified @"Simplified"
#define Traditional @"Traditional"
#define Korea @"Korea"
#define Japanese @"Japanese"

#define Codepage @"Codepage"

typedef NS_ENUM(NSInteger, ConnectType) {
    NONE = 0,   //None
    BT,         //Bluetooth
    WIFI,       //WiFi
};

typedef NS_ENUM(NSInteger, ENCRYPT) {
    ENCRYPT_NULL = 0,
    ENCRYPT_WEP64,
    ENCRYPT_WEP128,
    ENCRYPT_WPA_AES_PSK,
    ENCRYPT_WPA_TKIP_PSK,
    ENCRYPT_WPA_TKIP_AES_PSK,
    ENCRYPT_WP2_AES_PSK,
    ENCRYPT_WP2_TKIP,
    ENCRYPT_WP2_TKIP_AES_PSK,
    ENCRYPT_WPA_WPA2_MixedMode
};

@interface POSPrinterVC ()<UITextFieldDelegate, POSBLEManagerDelegate, POSWIFIManagerDelegate, CodePageViewDelegate>

// connect state tip
@property (weak, nonatomic) IBOutlet UILabel *connectStateLabel;

// wifi
@property (weak, nonatomic) IBOutlet UITextField *wifiTextField;

// connect type
@property (assign, nonatomic) ConnectType connectType;

// indicator
@property (strong, nonatomic) UIActivityIndicatorView *indicator;

// check status button
@property (weak, nonatomic) IBOutlet UIButton *checkStatusButton;

// disconnect button
@property (weak, nonatomic) IBOutlet UIButton *disconnectButton;

// scan bluetooth button
@property (weak, nonatomic) IBOutlet UIButton *blueScanButton;

// bluetooth config button
@property (weak, nonatomic) IBOutlet UIButton *blueConfigButton;

// connnect wifi button
@property (weak, nonatomic) IBOutlet UIButton *connectButton;

// language button
@property (weak, nonatomic) IBOutlet UIButton *languageButton;

// code page button
@property (weak, nonatomic) IBOutlet UIButton *codePageButton;

//dip button
@property (weak, nonatomic) IBOutlet UIButton *dipButton;

// density button
@property (weak, nonatomic) IBOutlet UIButton *densityButton;

// udp search button
@property (weak, nonatomic) IBOutlet UIButton *udpSearchButton;

// wifi config button
@property (weak, nonatomic) IBOutlet UIButton *wifiConfigButton;

// print text button
@property (weak, nonatomic) IBOutlet UIButton *printTextButton;

// print qrcode button
@property (weak, nonatomic) IBOutlet UIButton *printQRCodeButton;

// print barcode button
@property (weak, nonatomic) IBOutlet UIButton *printBarCodeButton;

// print picture button
@property (weak, nonatomic) IBOutlet UIButton *printPictureButton;

// language sheet
@property (strong, nonatomic) CodePagePopView *languagePopView;
@property (copy, nonatomic) NSArray *languageArray;

// code page sheet
@property (strong, nonatomic) CodePagePopView *codePagePopView;

// density sheet
@property (strong, nonatomic) CodePagePopView *densityPopView;
@property (copy, nonatomic) NSArray *densityArray;

// bluetooth manager
@property (strong, nonatomic) POSBLEManager *bleManager;

// wifi manager
@property (strong, nonatomic) POSWIFIManager *wifiManager;

@property (weak, nonatomic) IBOutlet UIButton *cashBoxButton;


@end

@implementation POSPrinterVC


#pragma mark - Life style

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self initView];
}

- (void)dealloc {
    [_bleManager removeDelegate:self];
    [_wifiManager removeDelegate:self];
    [self discount:nil];
}


#pragma mark - Private

- (void)initView {
    _indicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
    _indicator.center = self.view.center;
    [self.view addSubview:self.indicator];
    
    _checkStatusButton.layer.cornerRadius = 10.f;
    _disconnectButton.layer.cornerRadius = 10.f;
    
    _blueScanButton.layer.cornerRadius = 10.f;
    _blueConfigButton.layer.cornerRadius = 10.f;
    
    _connectButton.layer.cornerRadius = 10.f;
    
    _languageButton.layer.cornerRadius = 10.f;
    _codePageButton.layer.cornerRadius = 10.f;
    _dipButton.layer.cornerRadius = 10.f;
    _densityButton.layer.cornerRadius = 10.f;
    _udpSearchButton.layer.cornerRadius = 10.f;
    _wifiConfigButton.layer.cornerRadius = 10.f;
    
    _printTextButton.layer.cornerRadius = 10.f;
    _printQRCodeButton.layer.cornerRadius = 10.f;
    _printBarCodeButton.layer.cornerRadius = 10.f;
    _printPictureButton.layer.cornerRadius = 10.f;
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(keyboardDismiss)];
    self.view.userInteractionEnabled = YES;
    [self.view addGestureRecognizer:tap];
    
    _bleManager = [POSBLEManager sharedInstance];
    _bleManager.delegate = self;
    
    _wifiManager = [POSWIFIManager sharedInstance];
    _wifiManager.delegate = self;
    
    _wifiTextField.delegate = self;
}

- (void)buttonStateOn {
    _checkStatusButton.enabled = YES;
    _disconnectButton.enabled = YES;
    
    if (_bleManager.isConnecting) _blueConfigButton.enabled = YES;
    
    _languageButton.enabled = YES;
    _codePageButton.enabled = YES;
    _dipButton.enabled = YES;
    _densityButton.enabled = YES;
    
    if (_wifiManager.isConnect) _wifiConfigButton.enabled = YES;
    
    _printTextButton.enabled = YES;
    _printQRCodeButton.enabled = YES;
    _printBarCodeButton.enabled = YES;
    _printPictureButton.enabled = YES;
}

- (void)buttonStateOff {
    _checkStatusButton.enabled = NO;
    _disconnectButton.enabled = NO;
    
    _blueConfigButton.enabled = NO;
    
    _languageButton.enabled = NO;
    _codePageButton.enabled = NO;
    _dipButton.enabled = NO;
    _densityButton.enabled = NO;
    _wifiConfigButton.enabled = NO;
    
    _printTextButton.enabled = NO;
    _printQRCodeButton.enabled = NO;
    _printBarCodeButton.enabled = NO;
    _printPictureButton.enabled = NO;
}

- (void)keyboardDismiss {
    [self.wifiTextField resignFirstResponder];
}


#pragma mark - Action

// check status
- (IBAction)checkStatusAction:(UIButton *)sender {
    __weak typeof(self) weakSelf = self;
    switch (self.connectType) {
        case WIFI:
        {
            if ([_wifiManager printerIsConnect]) {
                [_wifiManager printerStatus:^(NSData *status) {
                    [weakSelf getStatusWithData:status];
                }];
            }
        }
            break;
            
        case BT:
        {
            if ([_bleManager printerIsConnect]) {
                [_bleManager printerStatus:^(NSData *status) {
                    [weakSelf getStatusWithData:status];
                }];
            }
        }
            break;
            
        default:
            break;
    }
    
}

- (void)getStatusWithData:(NSData *)responseData {
    
    if (responseData.length == 0) return;
    
    if (responseData.length == 1) {
        const Byte *byte = (Byte *)[responseData bytes];
        unsigned status = byte[0];
        
        if (status == 0x12) {
            [self.view makeToast:@"Ready" duration:1.f position:CSToastPositionCenter];
        } else if (status == 0x16) {
            [self.view makeToast:@"Cover opened" duration:1.f position:CSToastPositionCenter];
        } else if (status == 0x32) {
            [self.view makeToast:@"Paper end" duration:1.f position:CSToastPositionCenter];
        } else if (status == 0x36) {
            [self.view makeToast:@"Cover opened & Paper end" duration:1.f position:CSToastPositionCenter];
        } else {
            [self.view makeToast:@"error" duration:1.f position:CSToastPositionCenter];
        }
    }
}

- (IBAction)testCashBoxAction:(id)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil
                                                                         message:nil
                                                                  preferredStyle:UIAlertControllerStyleActionSheet];
    
    UIAlertAction *option1 = [UIAlertAction actionWithTitle:@"Open CashBox"
                                                      style:UIAlertActionStyleDefault
                                                    handler:^(UIAlertAction * _Nonnull action) {
        [self openCashBox];
        
    }];
    
    UIAlertAction *option2 = [UIAlertAction actionWithTitle:@"CashBox Status"
                                                      style:UIAlertActionStyleDefault
                                                    handler:^(UIAlertAction * _Nonnull action) {
        [self checkCashBoxStatus];
        
    }];
    
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"Cancel"
                                                           style:UIAlertActionStyleCancel
                                                         handler:^(UIAlertAction * _Nonnull action) {
                                                         }];
    
    [actionSheet addAction:option1];
    [actionSheet addAction:option2];
    [actionSheet addAction:cancelAction];
   
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        actionSheet.popoverPresentationController.sourceView = self.cashBoxButton;
        actionSheet.popoverPresentationController.sourceRect = CGRectMake(CGRectGetMidX(self.cashBoxButton.bounds), CGRectGetMidY(self.cashBoxButton.bounds), 0, 0);
    }
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}

- (void)checkCashBoxStatus {
    NSMutableData *dataM = [NSMutableData dataWithData:[POSCommand initializePrinter]];
    [dataM appendData:[POSCommand returnState:2]];
    [self printWithData:dataM];
}

- (void)openCashBox {
    NSMutableData *dataM = [NSMutableData dataWithData:[POSCommand initializePrinter]];
    [dataM appendData:[POSCommand creatCashBoxContorPulseWithM:0 andT1:30 andT2:255]];
    [self printWithData:dataM];
}


/**
 * receive printer data
 */
- (void)POSwifiReceiveValueForData:(NSData *)data {
    
    /// test Casher status
    if (data.length == 0) return;
    
    if (data.length == 1) {
        const Byte *byte = (Byte *)[data bytes];
        unsigned status = byte[0];
        
        if (status == 0x00) {
            [self.view makeToast:@"Cash box open" duration:1.f position:CSToastPositionCenter];
        } else if (status == 0x01) {
            [self.view makeToast:@"Cash box closed" duration:1.f position:CSToastPositionCenter];
        }
    }
    
}

// wifi connect
- (IBAction)wifiConnectAction:(UIButton *)sender {
    
    if (_wifiManager.isConnect) {
        [_wifiManager disconnect];
    }
    
    [_wifiManager connectWithHost:self.wifiTextField.text port:9100];
    
    [self keyboardDismiss];
}

//discount manual
- (IBAction)discount:(UIButton *)sender {
    switch (self.connectType) {
        case BT:
            [_bleManager disconnectRootPeripheral];
            break;
            
        case WIFI:
            [_wifiManager disconnect];
            break;
            
        default:
            break;
    }
}

// blue config
- (IBAction)blueConfigAction:(UIButton *)sender {
    [_bleManager setBluetoothNameAndKeyWith:@"Printer1234" btKey:@"1234"];
}

- (IBAction)languageAction:(UIButton *)sender {
    _languageArray = @[Simplified, Traditional, Korea, Japanese];
    _languagePopView = [CodePagePopView initViewWithArray:_languageArray];
    _languagePopView.delegate = self;
    _languagePopView.tag = 1012;
}

- (IBAction)codePageAction:(UIButton *)sender {
    NSBundle *bundle = [NSBundle mainBundle];
    NSString *path = [bundle pathForResource:@"code_page" ofType:@"plist"];
    NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile:path];
    NSArray *array = [dict allValues];
    
    _codePagePopView = [CodePagePopView initViewWithArray:array];
    _codePagePopView.delegate = self;
    _codePagePopView.tag = 1010;
}

- (IBAction)densityAction:(UIButton *)sender {
    _densityArray = @[@"Print Density 1", @"Print Density 2", @"Print Density 3", @"Print Density 4", @"Print Density 5", @"Print Density 6", @"Print Density 7", @"Print Density 8"];
    _densityPopView = [CodePagePopView initViewWithArray:_densityArray];
    _densityPopView.delegate = self;
    _densityPopView.tag = 1011;
}

- (IBAction)wifiConfigButton:(UIButton *)sender {
    [_wifiManager setWiFiConfigWithIP:@"192.168.91.23" mask:@"255.255.255.0" gateway:@"192.168.91.1" ssid:@"H3C_CFAECB" password:@"service123" encrypt:ENCRYPT_WP2_AES_PSK];
}


#pragma mark - CodePageViewDelegate

- (void)codepageView:(CodePagePopView *)codepagePopView selectValue:(NSString *)selectValue {
    
    switch (codepagePopView.tag) {
        case 1010:
        {
            [_codePageButton setTitle:selectValue forState:UIControlStateNormal];
            
            NSBundle *bundle = [NSBundle mainBundle];
            NSString *path = [bundle pathForResource:@"code_page" ofType:@"plist"];
            NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile:path];
            
            NSArray *keyArray = [dict allKeysForObject:selectValue];
            if ([keyArray firstObject]) {
                NSNumber *key = [keyArray firstObject];
                if (key) {
//                    NSLog(@"key = %@", key);
                    [_wifiManager writeCommandWithData:[POSCommand setCodePage:[key intValue]]];
                    [_codePagePopView hideView];
                }
            }
        }
            break;
            
        case 1011:
        {
            [_densityButton setTitle:selectValue forState:UIControlStateNormal];
            
            NSInteger index = [_densityArray indexOfObject:selectValue];
            int density = (int)(index + 1);
//            NSLog(@"%d", density);
            [_wifiManager writeCommandWithData:[POSCommand setDensity:density]];
            [_densityPopView hideView];
        }
            break;
            
        case 1012:
        {
            [_languageButton setTitle:selectValue forState:UIControlStateNormal];
            
            NSInteger index = [_languageArray indexOfObject:selectValue];
            [_wifiManager writeCommandWithData:[POSCommand setDoubleByteLanguageWithType:(int)index]];
            [_languagePopView hideView];
        }
            break;
            
        default:
            break;
    }
    
}


#pragma mark - UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}


#pragma mark - POSBLEManagerDelegate

//connect success
- (void)POSbleConnectPeripheral:(CBPeripheral *)peripheral {
    _connectType = BT;
    _connectStateLabel.text = @"POS";
    [self buttonStateOn];
}


// disconnect
- (void)POSbleDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error{
    if (_wifiManager.isConnect) {
        _connectType = WIFI;
        _connectStateLabel.text = @"WiFi";
    } else {
        _connectType = NONE;
        _connectStateLabel.text = @"NONE";
        
        [self buttonStateOff];
    }
}


#pragma mark - POSWIFIManagerDelegate

//connected success
- (void)POSwifiConnectedToHost:(NSString *)host port:(UInt16)port {
    _connectType = WIFI;
    _connectStateLabel.text = @"POS";
    
    [self buttonStateOn];
    [self.indicator stopAnimating];
    [self checkStatusAction:nil];
}

//disconnected
- (void)POSwifiDisconnectWithError:(NSError *)error {
    if (_bleManager.isConnecting) {
        _connectType = BT;
        _connectStateLabel.text = @"BLE";
    } else {
        _connectType = NONE;
        _connectStateLabel.text = @"NONE";
        
        [self buttonStateOff];
    }
    
    if (error) {
        [self.view makeToast:[NSString stringWithFormat:@"%@", error] duration:2.f position:CSToastPositionCenter];
    }
}


#pragma mark - Test Print

- (void)printWithData:(NSData *)printData {
    switch (self.connectType) {
        case NONE:
            [self.view makeToast:@"please connect printer" duration:1.f position:CSToastPositionCenter];
            break;
            
        case WIFI:
            [_wifiManager writeCommandWithData:printData];
            break;
            
        case BT:
            [_bleManager writeCommandWithData:printData writeCallBack:^(CBCharacteristic *characteristic, NSError *error) {
                if(!error) {
                    NSLog(@"send success");
                } else {
                    NSLog(@"error:%@",error);
                }
            }];
            break;
            
        default:
            break;
    }
}

- (IBAction)posTextAction:(id)sender {
    NSMutableData *dataM = [NSMutableData dataWithData:[POSCommand initializePrinter]];
    NSStringEncoding gbkEncoding = CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000);
    [dataM appendData:[POSCommand selectOrCancleUnderLineModel:1]];//下划线
    [dataM appendData:[POSCommand selectOrCancleBoldModel:1]];//加粗
    [dataM appendData:[POSCommand selectAlignment:POS_ALIGNMENT_CENTER]];//居中对齐
    [dataM appendData: [@"123456789abc" dataUsingEncoding: gbkEncoding]];
    [dataM appendData:[POSCommand printAndFeedLine]];
    [dataM appendData:[POSCommand selectAlignment:POS_ALIGNMENT_LEFT]];//居左对齐
    [dataM appendData:[POSCommand setTextSize:TXT_1WIDTH height:TXT_1HEIGHT]];//字符宽度放大一倍，高度放大一倍
    [dataM appendData: [@"123456789abc" dataUsingEncoding: gbkEncoding]];
    [dataM appendData:[POSCommand printAndFeedLine]];
    [dataM appendData:[POSCommand selectAlignment:POS_ALIGNMENT_RIGHT]];//居右对齐
    [dataM appendData: [POSCommand selectChineseCharacterModel]];
    [dataM appendData:[POSCommand setTextSize:TXT_DEFAULTWIDTH height:TXT_DEFAULTHEIGHT]];//默认大小
    [dataM appendData: [@"123456789abc" dataUsingEncoding: gbkEncoding]];
    [dataM appendData:[POSCommand printAndFeedLine]];
    
    /// new
    [dataM appendData:[POSCommand selectOrCancleUnderLineModel:0]];//取消下划线
    [dataM appendData:[POSCommand selectOrCancleBoldModel:0]];//取消加粗
    [dataM appendData:[POSCommand printTextSize:@"w1H0" textWid:TXT_1WIDTH textHei:TXT_DEFAULTHEIGHT]];
    [dataM appendData:[POSCommand printTextSize:@"w1H1" textWid:TXT_4WIDTH textHei:TXT_1HEIGHT]];
    [dataM appendData:[POSCommand printTextSize:@"w4H2" textWid:TXT_4WIDTH textHei:TXT_2HEIGHT]];
    [dataM appendData:[POSCommand printTextSize:@"w6H6" textWid:TXT_6WIDTH textHei:TXT_6HEIGHT]];
    [dataM appendData:[POSCommand printTextSize:@"w7H7" textWid:TXT_7WIDTH textHei:TXT_7HEIGHT]];
    [dataM appendData:[POSCommand printTextSize:@"w3H3" textWid:TXT_3WIDTH textHei:TXT_3HEIGHT]];
    [dataM appendData:[POSCommand printTextAlignment:@"LEFT1234" alignment:POS_ALIGNMENT_LEFT]];
    [dataM appendData:[POSCommand printTextAlignment:@"CENTER1234" alignment:POS_ALIGNMENT_CENTER]];
    [dataM appendData:[POSCommand printTextAlignment:@"RIGHT1234" alignment:POS_ALIGNMENT_RIGHT]];
    [dataM appendData:[POSCommand printTextAttribute:@"UNDERLINE1" attribute:FNT_UNDERLINE]];
    [dataM appendData:[POSCommand printTextAttribute:@"UNDERLINE2" attribute:FNT_UNDERLINE2]];
    [dataM appendData:[POSCommand printTextAttribute:@"FONTB" attribute:FNT_FONTB]];
    [dataM appendData:[POSCommand printTextAttribute:@"FNT_DEFAULT" attribute:FNT_DEFAULT]];
    [dataM appendData:[POSCommand printTextAttribute:@"BOLD" attribute:FNT_BOLD]];
    [dataM appendData:[POSCommand printTextAttribute:@"REVERSE" attribute:FNT_REVERSE]];
    [dataM appendData:[POSCommand printText:@"ABCD1234" alignment:POS_ALIGNMENT_CENTER attribute:FNT_REVERSE textWid:TXT_2WIDTH textHei:TXT_2HEIGHT]];
    
    [dataM appendData:[POSCommand printAndFeedForwardWhitN:6]];
    [self printWithData:dataM];
}

- (IBAction)posQRCodeAction:(id)sender {
    NSMutableData *dataM = [NSMutableData dataWithData:[POSCommand initializePrinter]];
    [dataM appendData:[POSCommand selectAlignment:1]];
    [dataM appendData:[POSCommand printQRCode:6 level:48 code:@"www.google.com" useEnCodeing:NSUTF8StringEncoding]];
    [dataM appendData:[POSCommand printAndFeedForwardWhitN:6]];
    [dataM appendData:[POSCommand selectCutPageModelAndCutpage:1]];
    
    [self printWithData:dataM];
}

- (IBAction)posBarCodeAction:(UIButton *)sender {
    NSMutableData *dataM = [NSMutableData dataWithData:[POSCommand initializePrinter]];
    
    NSStringEncoding gbkEncoding = CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000);
    
    [dataM appendData:[POSCommand selectHRICharactersPrintPosition:2]];
    [dataM appendData:[POSCommand selectAlignment:1]];
    [dataM appendData:[POSCommand setBarcodeHeight:70]];
    [dataM appendData: [@"UPC-A\n" dataUsingEncoding: gbkEncoding]];
    [dataM appendData:[POSCommand printBarcodeWithM:0 andContent:@"123456789012" useEnCodeing:NSUTF8StringEncoding]];
    [dataM appendData:[POSCommand printAndFeedLine]];
    [dataM appendData: [@"UPC-E\n" dataUsingEncoding: gbkEncoding]];
    [dataM appendData:[POSCommand printBarcodeWithM:1 andContent:@"042100005264" useEnCodeing:NSUTF8StringEncoding]];
    [dataM appendData:[POSCommand printAndFeedLine]];
    [dataM appendData: [@"JAN13\n" dataUsingEncoding: gbkEncoding]];
    [dataM appendData:[POSCommand printBarcodeWithM:2 andContent:@"123456791234" useEnCodeing:NSUTF8StringEncoding]];
    [dataM appendData:[POSCommand printAndFeedLine]];
    [dataM appendData: [@"JAN8\n" dataUsingEncoding: gbkEncoding]];
    [dataM appendData:[POSCommand printBarcodeWithM:3 andContent:@"12345678" useEnCodeing:NSUTF8StringEncoding]];
    [dataM appendData:[POSCommand printAndFeedLine]];
    [dataM appendData: [@"CODE39\n" dataUsingEncoding: gbkEncoding]];
    [dataM appendData:[POSCommand setBarcodeWidth:2]];
    [dataM appendData:[POSCommand printBarcodeWithM:4 andContent:@"ABCDEFGHI" useEnCodeing:NSUTF8StringEncoding]];
    [dataM appendData:[POSCommand printAndFeedLine]];
    [dataM appendData: [@"ITF\n" dataUsingEncoding: gbkEncoding]];
    [dataM appendData:[POSCommand printBarcodeWithM:5 andContent:@"123456789012" useEnCodeing:NSUTF8StringEncoding]];
    [dataM appendData:[POSCommand printAndFeedLine]];
    [dataM appendData: [@"CODEBAR\n" dataUsingEncoding: gbkEncoding]];
    [dataM appendData:[POSCommand printBarcodeWithM:6 andContent:@"A37859B" useEnCodeing:NSUTF8StringEncoding]];
    [dataM appendData:[POSCommand printAndFeedLine]];
    [dataM appendData: [@"CODE93\n" dataUsingEncoding: gbkEncoding]];
    [dataM appendData:[POSCommand printBarcodeWithM:72 andN:2 andContent:@"123456789" useEnCodeing:NSUTF8StringEncoding]];
    [dataM appendData:[POSCommand printAndFeedLine]];
    [dataM appendData: [@"CODE128\n" dataUsingEncoding: gbkEncoding]];
    [dataM appendData:[POSCommand printBarcodeWithM:73 andN:2 andContent:@"No.123456" useEnCodeing:NSUTF8StringEncoding]];
    [dataM appendData:[POSCommand printAndFeedLine]];
    
    [dataM appendData:[POSCommand printAndFeedForwardWhitN:6]];
    [dataM appendData:[POSCommand selectCutPageModelAndCutpage:1]];
    [self printWithData:dataM];
}

- (IBAction)posPictureAction:(id)sender {
    UIImage *img = [UIImage imageNamed:@"image"];
    
    NSMutableData *dataM = [NSMutableData dataWithData:[POSCommand initializePrinter]];
    [dataM appendData:[POSCommand selectAlignment:1]];
    [dataM appendData:[POSCommand printRasteBmpWithM:RasterNolmorWH andImage:img andType:Dithering]];
    [dataM appendData:[POSCommand printAndFeedForwardWhitN:6]];
    [dataM appendData:[POSCommand selectCutPageModelAndCutpage:1]];
    
    [self printWithData:dataM];
}

- (IBAction)print58mmDemoAction:(id)sender {
    NSMutableData *dataM = [NSMutableData dataWithData:[POSCommand initializePrinter]];
    NSStringEncoding enc = CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000);
    [dataM appendData:[POSCommand selectAlignment:1]];
    [dataM appendData:[POSCommand setTextSize:TXT_1WIDTH height:TXT_1HEIGHT]];
    [dataM appendData:[@"Hangzhou Cuiyuan Drinking Water Co., Ltd.\n" dataUsingEncoding:enc]];

    [dataM appendData:[POSCommand setTextSize:TXT_DEFAULTWIDTH height:TXT_DEFAULTHEIGHT]];
    [dataM appendData:[POSCommand selectAlignment:0]];

    [dataM appendData:[@"Document Number: 10GJK00001626\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"Customer Name: sxq\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"Salesman: Ban Xiang\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"Telephone:\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"Order Time: 2023-11-24 14:55:20\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"- - - - - - - - - - - - - - - -\n" dataUsingEncoding:enc]];

    [dataM appendData:[PTable addAutoTableH:@[@"Product", @"Quantity", @"Unit Price", @"Amount"] titleLength:@[@8, @8, @7, @7] align:FIRST_LEFT_ALIGN]];
    [dataM appendData:[@"1. 380 water 12 colored film\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addBarcodeRow:@"123456791234" type:BCS_EAN13 hri:BAR_HRI_TEXT_BOTH]];
    [dataM appendData:[PTable addAutoTableH:@[@"", @"10 boxes 5 bottles", @"23.37", @"243.45"] titleLength:@[@8, @8, @7, @7] align:FIRST_LEFT_ALIGN]];
    [dataM appendData:[@"Note: Look at how much phone calls there are at home\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"2. 308 water 24 cartons\n" dataUsingEncoding:enc]];
    [dataM appendData:[@"Barcode: 123456791234\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"", @"5 boxes", @"24.37", @"121.85"] titleLength:@[@8, @8, @7, @7] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"3. 308 water 24 white films\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Gross Profit Gift", @"1 box 1 bottle", @"26.8", @"0.00"] titleLength:@[@8, @8, @7, @7] align:FIRST_LEFT_ALIGN]];
    [dataM appendData:[@"Note: UK Momo Momo Momo Momo Momo\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"4. Special-380 water 24 cartons Yangtze River Pharmaceutical\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"", @"5 boxes", @"30.0", @"150.00"] titleLength:@[@8, @8, @7, @7] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"5. 550 water 24 cartons\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Set Main", @"5 boxes", @"24.0", @"120.00"] titleLength:@[@8, @8, @7, @7] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"6. 380 water 24 cartons\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Set Gift", @"2 bottles", @"24.37", @"2.04"] titleLength:@[@8, @8, @7, @7] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"7. 380 water 24 cartons\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Set Main", @"4 boxes", @"24.37", @"97.48"] titleLength:@[@8, @8, @7, @7] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"8. 380 water 24 films\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Set Gift", @"1 box", @"26.8", @"26.80"] titleLength:@[@8, @8, @7, @7] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"9. 19L bucket water\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Farmer Display November", @"1 box", @"45.0", @"0.00"] titleLength:@[@8, @8, @7, @7] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"10. Special-380 water 24 cartons Yangtze River Pharmaceutical\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Farmer Display November", @"6 boxes", @"30.0", @"0.00"] titleLength:@[@8, @8, @7, @7] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"- - - - - - - - - - - - - - - -\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"Total Amount: 732.78\n" dataUsingEncoding:enc]];
    [dataM appendData:[@"Total Quantity: 38 boxes 8 bottles\n" dataUsingEncoding:enc]];
    [dataM appendData:[@"Note: A lot of notes\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"- - - - - - - - - - - - - - - -\n" dataUsingEncoding:enc]];

    [dataM appendData:[POSCommand selectAlignment:1]];
    [dataM appendData:[@"Scan Payment\n" dataUsingEncoding:enc]];
    [dataM appendData:[POSCommand printQRCode:6 level:48 code:@"www.google.com" useEnCodeing:NSUTF8StringEncoding]];
    [dataM appendData:[POSCommand printAndFeedForwardWhitN:6]];
    [dataM appendData:[POSCommand selectCutPageModelAndCutpage:1]];
    [self printWithData:dataM];

}

- (IBAction)print80mmDemoAction:(id)sender {
    NSMutableData *dataM = [NSMutableData dataWithData:[POSCommand initializePrinter]];
    NSStringEncoding enc = CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000);
    [dataM appendData:[POSCommand selectAlignment:1]];
    [dataM appendData:[POSCommand setTextSize:TXT_1WIDTH height:TXT_1HEIGHT]];
    [dataM appendData:[@"Hangzhou Cuiyuan Drinking Water Co., Ltd.\n" dataUsingEncoding:enc]];

    [dataM appendData:[POSCommand setTextSize:TXT_DEFAULTWIDTH height:TXT_DEFAULTHEIGHT]];
    [dataM appendData:[POSCommand selectAlignment:0]];

    [dataM appendData:[@"Document Number: 10GJK00001626\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"Customer Name: sxq\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"Salesman: Ban Xiang\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"Telephone:\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"Order Time: 2023-11-24 14:55:20\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"- - - - - - - - - - - - - - - - - - - -\n" dataUsingEncoding:enc]];

    [dataM appendData:[PTable addAutoTableH:@[@"Item", @"Quantity", @"Unit Price", @"Amount"] titleLength:@[@12, @12, @10, @10] align:FIRST_LEFT_ALIGN]];
    [dataM appendData:[@"1. 380 water 12 colored film\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addBarcodeRow:@"123456791234" type:BCS_EAN13 hri:BAR_HRI_TEXT_ABOVE]];
    [dataM appendData:[PTable addAutoTableH:@[@"", @"10 boxes 5 bottles", @"23.37", @"243.45"] titleLength:@[@12, @12, @10, @10] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"Note: Look at how much phone calls there are at home\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"2. 308 water 24 cartons\n" dataUsingEncoding:enc]];
    [dataM appendData:[@"Barcode: 123456791234\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"", @"5 boxes", @"24.37", @"121.85"] titleLength:@[@12, @12, @10, @10] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"3. 308 water 24 white films\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Gross Profit Gift", @"1 box 1 bottle", @"26.8", @"0.00"] titleLength:@[@12, @12, @10, @10] align:FIRST_LEFT_ALIGN]];
    [dataM appendData:[@"Note: UK Momo Momo Momo Momo Momo\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"4. Special-380 water 24 cartons Yangtze River Pharmaceutical\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"", @"5 boxes", @"30.0", @"150.00"] titleLength:@[@12, @12, @10, @10] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"5. 550 water 24 cartons\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Set Main", @"5 boxes", @"24.0", @"120.00"] titleLength:@[@12, @12, @10, @10] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"6. 380 water 24 cartons\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Set Gift", @"2 bottles", @"24.37", @"2.04"] titleLength:@[@12, @12, @10, @10] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"7. 380 water 24 cartons\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Set Main", @"4 boxes", @"24.37", @"97.48"] titleLength:@[@12, @12, @10, @10] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"8. 380 water 24 films\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Set Gift", @"1 box", @"26.8", @"26.80"] titleLength:@[@12, @12, @10, @10] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"9. 19L bucket water\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Farmer Display November", @"1 box", @"45.0", @"0.00"] titleLength:@[@12, @12, @10, @10] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"10. Special-380 water 24 cartons Yangtze River Pharmaceutical\n" dataUsingEncoding:enc]];
    [dataM appendData:[PTable addAutoTableH:@[@"Farmer Display November", @"6 boxes", @"30.0", @"0.00"] titleLength:@[@12, @12, @10, @10] align:FIRST_LEFT_ALIGN]];

    [dataM appendData:[@"- - - - - - - - - - - - - - - - - - - -\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"Total Amount: 732.78\n" dataUsingEncoding:enc]];
    [dataM appendData:[@"Total Quantity: 38 boxes 8 bottles\n" dataUsingEncoding:enc]];
    [dataM appendData:[@"Note: Note Note Note Note\n" dataUsingEncoding:enc]];

    [dataM appendData:[@"- - - - - - - - - - - - - - - - - - - -\n" dataUsingEncoding:enc]];

    [dataM appendData:[POSCommand selectAlignment:1]];
    [dataM appendData:[@"Scan Payment\n" dataUsingEncoding:enc]];
    [dataM appendData:[POSCommand printQRCode:6 level:48 code:@"www.google.com" useEnCodeing:NSUTF8StringEncoding]];
    [dataM appendData:[POSCommand printAndFeedForwardWhitN:6]];
    [dataM appendData:[POSCommand selectCutPageModelAndCutpage:1]];
    [self printWithData:dataM];

}

@end
