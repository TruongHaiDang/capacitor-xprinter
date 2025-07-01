//
//  TSCPrinterVC.m
//  Printer
//

#import "TSCPrinterVC.h"
#import "TSCPrinterSDK.h"
#import "UIView+Toast.h"
typedef NS_ENUM(NSInteger, ConnectType) {
    NONE = 0,   //无连接
    BT,         //蓝牙
    WIFI,       //WiFi
};

@interface TSCPrinterVC ()<UITextFieldDelegate, TSCBLEManagerDelegate, TSCWIFIManagerDelegate>

@property (assign, nonatomic) ConnectType connectType;

//@property (strong, nonatomic) UIActivityIndicatorView *indicator;
@property (strong, nonatomic) UIView *noTouchView;
@property (assign, nonatomic) NSUInteger commandType;

@property (weak, nonatomic) IBOutlet UILabel *connectStateLabel;
@property (weak, nonatomic) IBOutlet UIButton *connectStateButton;
@property (weak, nonatomic) IBOutlet UIButton *disconnectButton;

@property (weak, nonatomic) IBOutlet UITextField *wifiTextField;

@property (weak, nonatomic) IBOutlet UIButton *printTextButton;
@property (weak, nonatomic) IBOutlet UIButton *printQRCodeButton;
@property (weak, nonatomic) IBOutlet UIButton *printBarcodeButton;
@property (weak, nonatomic) IBOutlet UIButton *printZplBarcode2Button;
@property (weak, nonatomic) IBOutlet UIButton *printPictureButton;
@property (weak, nonatomic) IBOutlet UIButton *printReverseButton;
@property (weak, nonatomic) IBOutlet UILabel *macAddressLb;
@property (weak, nonatomic) IBOutlet UIButton *getMacAddressButton;

@property (weak, nonatomic) IBOutlet UIButton *downPicButton;

@property (weak, nonatomic) IBOutlet UIButton *usePicButton;

@property (weak, nonatomic) IBOutlet UIButton *deletePicButton;

@property (weak, nonatomic) IBOutlet UILabel *directionLb;

@property (weak, nonatomic) IBOutlet UISwitch *directionSw;


// bluetooth manager
@property (strong, nonatomic) TSCBLEManager *bleManager;

// wifi manager
@property (strong, nonatomic) TSCWIFIManager *wifiManager;

@end

@implementation TSCPrinterVC

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self initView];
    //打开日志 默认关闭
//    [KDS_Log setLogEnable:YES];
}

- (void)dealloc {
    [_bleManager removeDelegate:self];
    [_wifiManager removeDelegate:self];
    [self discount:nil];
}


#pragma mark - Private

- (void)initView {
//    _indicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
//    _indicator.center = self.view.center;
//    [self.view addSubview:self.indicator];
    
    _noTouchView = [[UIView alloc] initWithFrame:self.view.bounds];
    _noTouchView.hidden = YES;
    [self.view addSubview:self.noTouchView];
    
    _printZplBarcode2Button.hidden = YES;
    _printReverseButton.hidden = YES;
    _downPicButton.hidden = YES;
    _usePicButton.hidden = YES;
    
    _deletePicButton.hidden = YES;
    _directionLb.hidden = YES;
    _directionSw.hidden = YES;
    
    _bleManager = [TSCBLEManager sharedInstance];
    _bleManager.delegate = self;
    
    _wifiManager = [TSCWIFIManager sharedInstance];
    _wifiManager.delegate = self;
    
    _wifiTextField.delegate = self;
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(keyboardDismiss)];
    self.view.userInteractionEnabled = YES;
    [self.view addGestureRecognizer:tap];
}

- (void)keyboardDismiss {
    [self.wifiTextField resignFirstResponder];
}

- (void)buttonStateOn {
    _connectStateButton.enabled = YES;
    _disconnectButton.enabled = YES;
    
    _printTextButton.enabled = YES;
    _printQRCodeButton.enabled = YES;
    _printBarcodeButton.enabled = YES;
    _printZplBarcode2Button.enabled = YES;
    _printPictureButton.enabled = YES;
    _printReverseButton.enabled = YES;
    _getMacAddressButton.enabled = YES;
    _usePicButton.enabled = YES;
    _downPicButton.enabled = YES;
    
    _deletePicButton.enabled = YES;
    _directionLb.enabled = YES;
    _directionSw.enabled = YES;
}

- (void)buttonStateOff {
    _connectStateButton.enabled = NO;
    _disconnectButton.enabled = NO;
    
    _printTextButton.enabled = NO;
    _printQRCodeButton.enabled = NO;
    _printBarcodeButton.enabled = NO;
    _printZplBarcode2Button.enabled = NO;
    _printPictureButton.enabled = NO;
    _printReverseButton.enabled = NO;
    _getMacAddressButton.enabled = NO;
    _usePicButton.enabled = NO;
    _downPicButton.enabled = NO;
    
    _deletePicButton.enabled = NO;
    _directionLb.enabled = NO;
    _directionSw.enabled = NO;
}


#pragma mark - Action


- (IBAction)checkStatusAction:(UIButton *)sender {
    
    switch (self.connectType) {
        case BT:
        {
            [_bleManager printerStatus:^(NSData *status) {
                [self toastWith:status];
            }];
        }
            break;
            
        case WIFI:
        {
            
            [_wifiManager printerStatus:^(NSData *status) {
                [self toastWith:status];
            }];
            
            break;
        }
            
        default:
            break;
    }
}

- (void)toastWith:(NSData *)data {

    unsigned status = 0;
    if (data.length == 1) {
        const Byte *byte = (Byte *)[data bytes];
        status = byte[0];
    } else if (data.length == 2) {
        const Byte *byte = (Byte *)[data bytes];
        status = byte[1];
    }
    
    if (status == 0x00) {
        [self.view makeToast:@"Ready" duration:1.f position:CSToastPositionCenter];
    } else if (status == 0x01) {
        [self.view makeToast:@"Cover opened" duration:1.f position:CSToastPositionCenter];
    } else if (status == 0x02) {
        [self.view makeToast:@"Paper jam" duration:1.f position:CSToastPositionCenter];
    } else if (status == 0x03) {
        [self.view makeToast:@"Cover opened and paper jam" duration:1.f position:CSToastPositionCenter];
    } else if (status == 0x04) {
        [self.view makeToast:@"Paper end" duration:1.f position:CSToastPositionCenter];
    } else if (status == 0x05) {
        [self.view makeToast:@"Cover opened and Paper end" duration:1.f position:CSToastPositionCenter];
    } else if (status == 0x08) {
        [self.view makeToast:@"No Ribbon" duration:1.f position:CSToastPositionCenter];
    } else if (status == 0x09) {
        [self.view makeToast:@"Cover opened and no Ribbon" duration:1.f position:CSToastPositionCenter];
    } else if (status == 0x10) {
        [self.view makeToast:@"Pause" duration:1.f position:CSToastPositionCenter];
    } else if (status == 0x20) {
        [self.view makeToast:@"Printing.." duration:1.f position:CSToastPositionCenter];
    }
}

- (IBAction)wifiConnectAction:(UIButton *)sender {
    if (self.wifiTextField.text.length == 0) {
        [self.view makeToast:@"please input wifi address" duration:1.f position:CSToastPositionCenter];
    }
    
    if (_wifiManager.isConnect) {
        [_wifiManager disconnect];
    }
    
    [self keyboardDismiss];
//    [self.indicator startAnimating];
    [_wifiManager connectWithHost:self.wifiTextField.text port:9100];
}

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

- (IBAction)commandControlAction:(UISegmentedControl *)sender {
    _commandType = sender.selectedSegmentIndex;
    
    switch (self.commandType) {
        case 0:
        {
            NSLog(@"TSPL");
            _printZplBarcode2Button.hidden = YES;
            _printReverseButton.hidden = YES;
            _downPicButton.hidden = YES;
            _usePicButton.hidden = YES;
            
            _deletePicButton.hidden = YES;
            _directionLb.hidden = YES;
            _directionSw.hidden = YES;
            
            
        }
            break;
            
        case 1:
        {
            NSLog(@"ZPL");
            _printZplBarcode2Button.hidden = NO;
            _printReverseButton.hidden = NO;
            _downPicButton.hidden = NO;
            _usePicButton.hidden = NO;
            
            _deletePicButton.hidden = NO;
            _directionLb.hidden = NO;
            _directionSw.hidden = NO;
        }
            break;
            
        case 2:
        {
            NSLog(@"CPCL");
            _printZplBarcode2Button.hidden = YES;
            _printReverseButton.hidden = NO;
            _downPicButton.hidden = YES;
            _usePicButton.hidden = YES;
            
            _deletePicButton.hidden = YES;
            _directionLb.hidden = YES;
            _directionSw.hidden = YES;
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


#pragma mark - TSCBLEManagerDelegate

- (void)TSCbleConnectPeripheral:(CBPeripheral *)peripheral {
    _connectType = BT;
    _connectStateLabel.text = peripheral.name;
    
    [self buttonStateOn];
}

- (void)TSCbleDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {

    if (_wifiManager.isConnect) {
        _connectType = WIFI;
        _connectStateLabel.text = @"WiFi";
    } else {
        _connectType = NONE;
        _connectStateLabel.text = @"NONE";
        
        [self buttonStateOff];
    }
    
    if (error.code == 6) {
        [_bleManager connectDevice:peripheral];
    }
}


#pragma mark - TSCWIFIManagerDelegate

// 成功连接主机
- (void)TSCwifiConnectedToHost:(NSString *)host port:(UInt16)port {
    _connectType = WIFI;
    _connectStateLabel.text = host;
    
    [self buttonStateOn];
    
    [self checkStatusAction:nil];
}

// 遇到错误关闭连接
- (void)TSCwifiDisconnectWithError:(NSError *)error {
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

- (IBAction)labelTextClick:(id)sender {
    NSMutableData *dataM = [[NSMutableData alloc] init];
    NSStringEncoding gbkEncoding = CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000);
    
    switch (self.commandType) {
            //tspl
        case 0:
        {
            [dataM appendData:[TSCCommand sizeBymmWithWidth:70 andHeight:85]];
            [dataM appendData:[TSCCommand gapBymmWithWidth:2 andHeight:0]];
            [dataM appendData:[TSCCommand cls]];
            [dataM appendData:[TSCCommand textWithX:0 andY:10 andFont:@"2" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"12 x 20 font" usStrEnCoding:gbkEncoding]];
            [dataM appendData:[TSCCommand textWithX:0 andY:30 andFont:@"2" andRotation:0 andX_mul:2 andY_mul:2 andContent:@"12 x 20 font" usStrEnCoding:gbkEncoding]];
            [dataM appendData:[TSCCommand textWithX:0 andY:80 andFont:@"3" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"16 x 24  font" usStrEnCoding:gbkEncoding]];
            [dataM appendData:[TSCCommand textWithX:0 andY:160 andFont:@"4" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"24 x 32 font" usStrEnCoding:gbkEncoding]];
            [dataM appendData:[TSCCommand textWithX:0 andY:240 andFont:@"5" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"32 x 48 font" usStrEnCoding:gbkEncoding]];
            [dataM appendData:[TSCCommand textWithX:0 andY:320 andFont:@"6" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"14 x 19 font" usStrEnCoding:gbkEncoding]];
            [dataM appendData:[TSCCommand textWithX:0 andY:400 andFont:@"7" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"21 x 27 font" usStrEnCoding:gbkEncoding]];
            [dataM appendData:[TSCCommand textWithX:0 andY:480 andFont:@"8" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"14 x25 font" usStrEnCoding:gbkEncoding]];
            [dataM appendData:[TSCCommand print:1]];
        }
            break;
            
            //zpl
        case 1:
        {
            [dataM appendData:[ZPLCommand XA]];
            [dataM appendData:[ZPLCommand setLabelWidth:560]];
            [dataM appendData:[ZPLCommand setCustomFont:@"LZHONGHEI" extension:@"TTF" alias:@"1" codePage:CODE_PAGE_UTF8]];
            [dataM appendData:[ZPLCommand drawTextWithx:20 y:100 customFontName:@"1" hSize:24 wSize:24 content:@"自定义字体LZHONGHEI"]];
            
            [dataM appendData:[ZPLCommand drawTextWithx:20 y:200 content:@"Default FNT_26_13"]];
            [dataM appendData:[ZPLCommand drawTextWithx:20 y:300 fontName:FNT_9_5 hRatio:2 wRatio:2 content:@"FNT_9_5 Ratio:2"]];
            [dataM appendData:[ZPLCommand drawTextWithx:20 y:400 fontName:FNT_28_24 rotation:ROTATION_90 hRatio:1 wRatio:1 content:@"FNT_20_18 Rotation:90"]];
            [dataM appendData:[ZPLCommand drawTextWithx:150 y:500 fontName:FNT_20_18 rotation:ROTATION_180 hRatio:1 wRatio:1 content:@"FNT_28_24 Rotation:180"]];
            [dataM appendData:[ZPLCommand drawTextWithx:350 y:400 fontName:FNT_18_10 rotation:ROTATION_270 hRatio:1 wRatio:1 content:@"FNT_34_22 Rotation:270"]];
            [dataM appendData:[ZPLCommand XZ]];
        }
            break;
            
            //cpcl
        case 2:
        {
            [CPCLCommand setStringEncoding:gbkEncoding];
            [dataM appendData:[CPCLCommand initLabelWithHeight:250 count:1 offsetx:0]];
            [dataM appendData:[CPCLCommand drawTextWithx:50 y:0 rotation:ROTA_0 font:FNT_0 content:@"FNT_0字体"]];
            [dataM appendData:[CPCLCommand drawTextWithx:50 y:50 rotation:ROTA_0 font:FNT_1 content:@"FNT_1字体"]];
            [dataM appendData:[CPCLCommand drawTextWithx:50 y:100 rotation:ROTA_0 font:FNT_2 content:@"FNT_2字体"]];
            [dataM appendData:[CPCLCommand drawTextWithx:200 y:0 rotation:ROTA_0 font:FNT_3 content:@"FNT_3字体"]];
            [dataM appendData:[CPCLCommand drawTextWithx:200 y:50 rotation:ROTA_0 font:FNT_4 content:@"FNT_4字体"]];
            [dataM appendData:[CPCLCommand drawTextWithx:200 y:100 rotation:ROTA_0 font:FNT_5 content:@"FNT_5字体"]];
            [dataM appendData:[CPCLCommand drawTextWithx:350 y:0 rotation:ROTA_0 font:FNT_6 content:@"FNT_6字体"]];
            [dataM appendData:[CPCLCommand drawTextWithx:350 y:50 rotation:ROTA_0 font:FNT_7 content:@"FNT_7字体"]];
            [dataM appendData:[CPCLCommand drawTextWithx:350 y:100 rotation:ROTA_0 font:FNT_24 content:@"FNT_24字体"]];
            [dataM appendData:[CPCLCommand drawTextWithx:350 y:150 rotation:ROTA_0 font:FNT_55 content:@"FNT_55字体"]];
            
            [dataM appendData:[CPCLCommand drawTextWithx:200 y:200 rotation:ROTA_90 font:FNT_0 content:@"旋转90"]];
            [dataM appendData:[CPCLCommand drawTextWithx:200 y:200 rotation:ROTA_180 font:FNT_0 content:@"旋转180"]];
            [dataM appendData:[CPCLCommand drawTextWithx:200 y:200 rotation:ROTA_270 font:FNT_0 content:@"旋转270"]];
            
            [dataM appendData:[CPCLCommand form]];
            [dataM appendData:[CPCLCommand print]];
        }
            break;
            
        default:
            break;
    }
    
    [self printData2Printer:dataM];
}

- (IBAction)labelBarcodeClick:(id)sender {
    NSMutableData *dataM = [[NSMutableData alloc] init];
    
    switch (self.commandType) {
            //tspl
        case 0:
        {
            [dataM appendData:[TSCCommand sizeBymmWithWidth:80 andHeight:100]];
            [dataM appendData:[TSCCommand gapBymmWithWidth:2 andHeight:0]];
            [dataM appendData:[TSCCommand cls]];
            [dataM appendData:[TSCCommand barcodeWithX:100 andY:50 andCodeType:@"128" andHeight:80 andHunabReadable:2 andRotation:0 andNarrow:2 andWide:2 andContent:@"12345678" usStrEnCoding:NSUTF8StringEncoding]];
            [dataM appendData:[TSCCommand print:1]];
        }
            break;
            
            //zpl
        case 1:
        {
            [dataM appendData:[ZPLCommand XA]];
            [dataM appendData:[ZPLCommand setLabelWidth:560]];
            [dataM appendData:[ZPLCommand drawTextWithx:0 y:100 content:@"Code 11"]];
            [dataM appendData:[ZPLCommand drawBarcodeWithx:0 y:150 codeType:CODE_TYPE_11 text:@"123456"]];
            
            [dataM appendData:[ZPLCommand drawTextWithx:250 y:100 content:@"Interleaved 2 of 5"]];
            [dataM appendData:[ZPLCommand drawBarcodeWithx:250 y:150 codeType:CODE_TYPE_25 text:@"123456"]];
            
            [dataM appendData:[ZPLCommand drawTextWithx:0 y:250 content:@"Code 39"]];
            [dataM appendData:[ZPLCommand drawBarcodeWithx:0 y:300 codeType:CODE_TYPE_39 text:@"123ABC"]];
            
            [dataM appendData:[ZPLCommand drawTextWithx:0 y:400 content:@"EAN 8"]];
            [dataM appendData:[ZPLCommand drawBarcodeWithx:0 y:450 codeType:CODE_TYPE_EAN8 text:@"1234567"]];
            
            [dataM appendData:[ZPLCommand drawTextWithx:250 y:400 content:@"UPC-E"]];
            [dataM appendData:[ZPLCommand drawBarcodeWithx:250 y:450 codeType:CODE_TYPE_UPCE text:@"1230000045"]];
            
            [dataM appendData:[ZPLCommand drawTextWithx:0 y:550 content:@"Code 93"]];
            [dataM appendData:[ZPLCommand drawBarcodeWithx:0 y:600 codeType:CODE_TYPE_93 text:@"12345ABCDE"]];
            
            [dataM appendData:[ZPLCommand XZ]];
        }
            break;
            
            //cpcl
        case 2:
        {
            [dataM appendData:[CPCLCommand initLabelWithHeight:600 count:1]];
            [dataM appendData:[CPCLCommand barcodeText:5]];
            
            [dataM appendData:[CPCLCommand drawTextWithx:50 y:0 content:@"Code 128"]];
            [dataM appendData:[CPCLCommand drawBarcodeWithx:50 y:30 codeType:BC_128 height:50 ratio:BCR_RATIO_1 content:@"123456789"]];
            
            [dataM appendData:[CPCLCommand drawTextWithx:300 y:40 content:@"UPC-A"]];
            [dataM appendData:[CPCLCommand drawBarcodeWithx:300 y:70 codeType:BC_UPCA height:50 ratio:BCR_RATIO_1 content:@"1234567890123"]];
            
            [dataM appendData:[CPCLCommand drawTextWithx:50 y:100 content:@"UPC-E"]];
            [dataM appendData:[CPCLCommand drawBarcodeWithx:50 y:130 codeType:BC_UPCE height:50 ratio:BCR_RATIO_1 content:@"223456"]];
            
            [dataM appendData:[CPCLCommand drawTextWithx:300 y:180 content:@"EAN/JAN-8"]];
            [dataM appendData:[CPCLCommand drawBarcodeWithx:300 y:210 codeType:BC_EAN8 height:50 ratio:BCR_RATIO_1 content:@"52233449"]];
            
            [dataM appendData:[CPCLCommand drawTextWithx:50 y:200 content:@"EAN/JAN-13"]];
            [dataM appendData:[CPCLCommand drawBarcodeWithx:50 y:230 codeType:BC_EAN13 height:50 ratio:BCR_RATIO_1 content:@"1234567890123"]];
            
            [dataM appendData:[CPCLCommand drawTextWithx:50 y:350 content:@"Code 39"]];
            [dataM appendData:[CPCLCommand drawBarcodeWithx:50 y:380 codeType:BC_39 height:50 ratio:BCR_RATIO_1 content:@"72233445"]];
            
            [dataM appendData:[CPCLCommand drawTextWithx:50 y:470 content:@"Code Code93/Ext.93"]];
            [dataM appendData:[CPCLCommand drawBarcodeWithx:50 y:500 codeType:BC_93 height:50 ratio:BCR_RATIO_1 content:@"823456789"]];
            
            [dataM appendData:[CPCLCommand drawTextWithx:350 y:360 content:@"CODABAR"]];
            [dataM appendData:[CPCLCommand drawBarcodeVerticalWithx:350 y:550 codeType:BC_CODABAR height:50 ratio:BCR_RATIO_1 content:@"A67859B"]];
            
            [dataM appendData:[CPCLCommand form]];
            [dataM appendData:[CPCLCommand print]];
        }
            break;
            
        default:
            break;
    }
    
    [self printData2Printer:dataM];
}

- (IBAction)zplBarcode2Action:(UIButton *)sender {
    NSMutableData *dataM = [[NSMutableData alloc] init];
    
    [dataM appendData:[ZPLCommand XA]];
    [dataM appendData:[ZPLCommand setLabelWidth:560]];
    
    [dataM appendData:[ZPLCommand drawTextWithx:0 y:100 content:@"Code 128"]];
    [dataM appendData:[ZPLCommand drawBarcodeWithx:0 y:150 codeType:CODE_TYPE_128 text:@"123456"]];
    
    [dataM appendData:[ZPLCommand drawTextWithx:250 y:100 content:@"EAN 13"]];
    [dataM appendData:[ZPLCommand drawBarcodeWithx:250 y:150 codeType:CODE_TYPE_EAN13 text:@"12345678"]];
    
    [dataM appendData:[ZPLCommand drawTextWithx:0 y:250 content:@"Codabar"]];
    [dataM appendData:[ZPLCommand drawBarcodeWithx:0 y:300 codeType:CODE_TYPE_CODA text:@"123456"]];
    
    [dataM appendData:[ZPLCommand drawTextWithx:250 y:250 content:@"MSI"]];
    [dataM appendData:[ZPLCommand drawBarcodeWithx:250 y:300 codeType:CODE_TYPE_MSI text:@"123456"]];
    
    [dataM appendData:[ZPLCommand drawTextWithx:0 y:400 content:@"PLESSEY"]];
    [dataM appendData:[ZPLCommand drawBarcodeWithx:0 y:450 codeType:CODE_TYPE_PLESSEY text:@"12345"]];
    
    [dataM appendData:[ZPLCommand drawTextWithx:0 y:550 content:@"UPC-A"]];
    [dataM appendData:[ZPLCommand drawBarcodeWithx:0 y:600 codeType:CODE_TYPE_UPCA text:@"04414"]];
    
    [dataM appendData:[ZPLCommand XZ]];
    
    [self printData2Printer:dataM];
}


- (IBAction)labelQRCodeClick:(id)sender {
    NSMutableData *dataM = [[NSMutableData alloc] init];
    
    switch (self.commandType) {
            //tspl
        case 0:
        {
            [dataM appendData:[TSCCommand sizeBymmWithWidth:80 andHeight:100]];
            [dataM appendData:[TSCCommand gapBymmWithWidth:2 andHeight:0]];
            [dataM appendData:[TSCCommand cls]];
            [dataM appendData:[TSCCommand qrCodeWithX:280 andY:10 andEccLevel:@"M" andCellWidth:8 andMode:@"A" andRotation:0 andContent:@"www.google.com" usStrEnCoding:NSUTF8StringEncoding]];
            [dataM appendData:[TSCCommand print:1]];
        }
            break;
            
            //zpl
        case 1:
        {
            [dataM appendData:[ZPLCommand XA]];
            [dataM appendData:[ZPLCommand setLabelWidth:520]];
            [dataM appendData:[ZPLCommand drawBoxWithx:0 y:100 width:200 height:400 thickness:5]];
            [dataM appendData:[ZPLCommand drawQRCodeWithx:50 y:150 factor:6 text:@"0123456789ABCD 2D code"]];
            [dataM appendData:[ZPLCommand XZ]];
        }
            break;
            
            //cpcl
        case 2:
        {
            [dataM appendData:[CPCLCommand initLabelWithHeight:300 count:1 offsetx:0]];
            [dataM appendData:[CPCLCommand drawLineWithx:50 y:10 xend:300 yend:10 width:5]];
            [dataM appendData:[CPCLCommand drawLineWithx:50 y:10 xend:50 yend:300 width:5]];
            [dataM appendData:[CPCLCommand drawQRCodeWithx:60 y:20 codeModel:CODE_MODE_ENHANCE cellWidth:6 content:@"ABC123"]];
            [dataM appendData:[CPCLCommand drawLineWithx:50 y:300 xend:300 yend:300 width:5]];
            [dataM appendData:[CPCLCommand drawLineWithx:300 y:10 xend:300 yend:300 width:5]];
            [dataM appendData:[CPCLCommand form]];
            [dataM appendData:[CPCLCommand print]];
        }
            break;
            
        default:
            break;
    }
    
    [self printData2Printer:dataM];
}

- (IBAction)labelPictureClick:(id)sender {
    NSMutableData *dataM = [[NSMutableData alloc] init];
    
    UIImage *image = [UIImage imageNamed:@"image"];
    switch (self.commandType) {
            //tspl
        case 0:
        {
            [dataM appendData:[TSCCommand sizeBymmWithWidth:80 andHeight:50]];
            [dataM appendData:[TSCCommand gapBymmWithWidth:0 andHeight:0]];
            [dataM appendData:[TSCCommand cls]];
            [dataM appendData:[TSCCommand bitmapWithX:0 andY:0 andMode:0 andImage:image]];
            [dataM appendData:[TSCCommand print:1]];
        }
            break;
            
            //zpl
        case 1:
        {
            [dataM appendData:[ZPLCommand XA]];
            [dataM appendData:[ZPLCommand setLabelWidth:520]];
            [dataM appendData:[ZPLCommand drawBoxWithx:0 y:100 width:360 height:280 thickness:10]];
            [dataM appendData:[ZPLCommand drawImageWithx:0 y:100 image:image]];
            [dataM appendData:[ZPLCommand XZ]];
        }
            break;
            
            //cpcl
        case 2:
        {
            [dataM appendData:[CPCLCommand initLabelWithHeight:640 count:1 offsetx:0]];
            [dataM appendData:[CPCLCommand drawBoxWithx:0 y:0 width:360 height:280 thickness:10]];
            [dataM appendData:[CPCLCommand drawImageWithx:0 y:0 image:image]];
            [dataM appendData:[CPCLCommand form]];
            [dataM appendData:[CPCLCommand print]];
        }
            break;
            
        default:
            break;
    }
    
    [self printData2Printer:dataM];
}

- (IBAction)labelReverseClick:(UIButton *)sender {
    NSMutableData *dataM = [[NSMutableData alloc] init];
    
    switch (self.commandType) {
            //zpl
        case 1:
        {
            [dataM appendData:[ZPLCommand XA]];
            [dataM appendData:[ZPLCommand drawReverseColorWithx:10 y:80 width:150 height:50 radius:0]];
            [dataM appendData:[ZPLCommand drawTextWithx:50 y:100 content:@"LABEL REVERSE"]];
            [dataM appendData:[ZPLCommand XZ]];
        }
            break;
            
            //cpcl
        case 2:
        {
            [dataM appendData:[CPCLCommand initLabelWithHeight:640 count:1 offsetx:0]];
            [dataM appendData:[CPCLCommand drawTextWithx:50 y:100 content:@"LABEL REVERSE"]];
            [dataM appendData:[CPCLCommand drawInverseLineWithx:40 y:80 xend:150 yend:80 width:80]];
            [dataM appendData:[CPCLCommand form]];
            [dataM appendData:[CPCLCommand print]];
        }
            break;
            
        default:
            break;
    }
    
    [self printData2Printer:dataM];
}

- (void)printData2Printer:(NSMutableData *)dataM {
    
    switch (self.connectType) {
        case BT:
        {
            self.noTouchView.hidden = NO;
            __weak typeof(self) weakSelf = self;
            [_bleManager writeCommandWithData:dataM writeCallBack:^(CBCharacteristic *characteristic, NSError *error) {
                weakSelf.noTouchView.hidden = YES;
                if (error) {
                    NSLog(@"%@", error);
                    return;
                }
            }];
        }
            break;
            
        case WIFI:
        {
            [_wifiManager writeCommandWithData:dataM];
        }
            break;
            
        default:
            [self.view makeToast:@"printer no connect" duration:1.f position:CSToastPositionCenter];
            break;
    }
}

- (IBAction)labelDownPicClick:(UIButton *)sender {
    NSMutableData *dataM = [[NSMutableData alloc] init];
    UIImage *image = [UIImage imageNamed:@"image"];
    [dataM appendData:[ZPLCommand XA]];
    [dataM appendData:[ZPLCommand downloadGraphic:@"R" name:@"SAMPLE1" image:image]];
    [dataM appendData:[ZPLCommand XZ]];
    [self printData2Printer:dataM];
}

- (IBAction)labelUsePicClick:(UIButton *)sender {
    NSMutableData *dataM = [[NSMutableData alloc] init];
    [dataM appendData:[ZPLCommand XA]];
    [dataM appendData:[ZPLCommand printGraphic:0 y:0 source:@"R" name:@"SAMPLE1" xMagnification:1 yMagnification:1]];
    [dataM appendData:[ZPLCommand XZ]];
    [self printData2Printer:dataM];
}

- (IBAction)deletePicClick:(UIButton *)sender {
    NSMutableData *dataM = [[NSMutableData alloc] init];
    [dataM appendData:[ZPLCommand XA]];
    [dataM appendData:[ZPLCommand deleteDownloadGraphic:@"R" name:@"SAMPLE1"]];
    [dataM appendData:[ZPLCommand XZ]];
    [self printData2Printer:dataM];
}

- (IBAction)direction:(UISwitch *)sender {
    NSMutableData *dataM = [[NSMutableData alloc] init];
    [dataM appendData:[ZPLCommand XA]];
    [dataM appendData:[ZPLCommand direction:sender.on]];
    [dataM appendData:[ZPLCommand XZ]];
    [self printData2Printer:dataM];
}


@end
