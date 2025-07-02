//
//  WIFIViewController.m
//  Printer
//
//  Created by Apple Mac mini intel on 2023/9/4.
//  Copyright © 2023 Admin. All rights reserved.
//

#import "WIFIViewController.h"
#import "TestWIFIConnecter.h"
#import "POSPrinterSDK.h"
#import "TSCPrinterSDK.h"
#import "UIView+Toast.h"

@interface WIFIViewController ()<UITableViewDelegate, UITableViewDataSource, UITextFieldDelegate, UIPickerViewDelegate, UIPickerViewDataSource, WIFIConnecterDelegate>
@property (weak, nonatomic) IBOutlet UIButton *checkStatusButton;

@property (weak, nonatomic) IBOutlet UIButton *disconnectButton;
@property (weak, nonatomic) IBOutlet UITextField *macAddressTF;
@property (weak, nonatomic) IBOutlet UITableView *connectButton;
@property (weak, nonatomic) IBOutlet UIButton *printTextButton;
@property (weak, nonatomic) IBOutlet UIButton *printQRCodeButton;
@property (weak, nonatomic) IBOutlet UIButton *printBarCodeButton;
@property (weak, nonatomic) IBOutlet UIButton *printPictureButton;
@property (weak, nonatomic) IBOutlet UITableView *macAddressTableView;
@property (weak, nonatomic) IBOutlet UIPickerView *connectedPickerView;
@property (weak, nonatomic) IBOutlet UIButton *printMultipleCopiesButton;
@property (strong, nonatomic) NSMutableArray *udpList;
@property (strong, nonatomic) NSMutableArray<TestWIFIConnecter *> *connectedPrinterList;
@property (assign, nonatomic) NSInteger mode;//0: POS，1:Label
@end


@implementation WIFIViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.

    [self initView];
    [self initSearchPrinter];
}

#pragma mark - lazy
- (NSMutableArray *)connectedPrinterList {
    if (!_connectedPrinterList) {
        _connectedPrinterList = [NSMutableArray new];
    }
    return _connectedPrinterList;
}

-(void)dealloc {
    NSLog(@"vc dealloc");
}

- (void)initView {
    self.checkStatusButton.layer.cornerRadius = 10;
    self.disconnectButton.layer.cornerRadius = 10;
    
    self.connectButton.layer.cornerRadius = 10;

    self.printTextButton.layer.cornerRadius = 10;
    self.printQRCodeButton.layer.cornerRadius = 10;
    self.printBarCodeButton.layer.cornerRadius = 10;
    self.printPictureButton.layer.cornerRadius = 10;
    self.printMultipleCopiesButton.layer.cornerRadius = 10;
    
    self.macAddressTF.delegate = self;
    self.macAddressTableView.delegate = self;
    self.macAddressTableView.dataSource = self;
    self.connectedPickerView.delegate = self;
    self.connectedPickerView.dataSource = self;
}

- (void)initSearchPrinter {
    self.udpList = [NSMutableArray array];
   [self sendFindCmd];
}

- (void)sendFindCmd {
    
    if ([[POSWIFIManager sharedInstance] createUdpSocket]) {
        
        if (self.udpList.count > 0) {
            [self.udpList removeAllObjects];
        }
        
        __weak typeof(self) weakSelf = self;
        [[POSWIFIManager sharedInstance] sendFindCmd:^(PrinterProfile *printer) {
            
            for (int i = 0; i < weakSelf.udpList.count; i++) {
                PrinterProfile *profile = weakSelf.udpList[i];
                if ([profile.printerName isEqualToString:printer.printerName]) {
                    return;
                }
            }
            [weakSelf.udpList addObject:printer];
            [weakSelf.macAddressTableView reloadData];
            
        }];
    }
    
}

// MARK: Private
- (void)buttonStatusOn {
    self.checkStatusButton.enabled = YES;
    self.disconnectButton.enabled = YES;
    self.printTextButton.enabled = YES;
    self.printQRCodeButton.enabled = YES;
    self.printBarCodeButton.enabled = YES;
    self.printPictureButton.enabled = YES;
    self.printMultipleCopiesButton.enabled = YES;
}

- (void)buttonStatusOff {
    self.checkStatusButton.enabled = NO;
    self.disconnectButton.enabled = NO;
    self.printTextButton.enabled = NO;
    self.printQRCodeButton.enabled = NO;
    self.printBarCodeButton.enabled = NO;
    self.printPictureButton.enabled = NO;
    self.printMultipleCopiesButton.enabled = NO;
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

- (void)printWithData:(NSData *)printData {
    NSInteger index  = [self.connectedPickerView selectedRowInComponent:0];
    if (self.connectedPrinterList.count > index) {
        TestWIFIConnecter * printer = self.connectedPrinterList[index];
        [printer writeCommandWithData:printData];
    }
}


// MARK: Action
- (IBAction)modeSelectAction:(UISegmentedControl *)sender {
    self.mode = sender.selectedSegmentIndex;
}

- (IBAction)connectAction:(UIButton *)sender {
    
    NSString * mStr = self.macAddressTF.text;
    if (mStr.length < 12) return;
    
    BOOL isExistPrinter = NO;
    for (TestWIFIConnecter *printer in self.connectedPrinterList) {
        if ([printer.deviceMac isEqualToString:mStr]) {
            isExistPrinter = YES;
        }
    }
    
    if (!isExistPrinter) {
        TestWIFIConnecter *printer = [[TestWIFIConnecter alloc] init];
        printer.delegate = self;
        [self.connectedPrinterList addObject:printer];
        
        for (int i = 0; i < self.udpList.count; i++) {
            PrinterProfile *profile = self.udpList[i];
            if ([profile.printerName isEqualToString: mStr]) {
                
                // method 1
                [printer connectWithDevice:profile];
                
                // method 2
                //[printer connectWithHost:[profile getIPString] port:9100];
                
                // method 3
                //printer.deviceIP = [profile getIPString];
                //[printer connectWithMac:profile.printerName];
                
                break;
            }
        }
    }
    
}

- (IBAction)disconnectAction:(UIButton *)sender {
    NSInteger index  = [self.connectedPickerView selectedRowInComponent:0];
    if (self.connectedPrinterList.count > index) {
        TestWIFIConnecter * printer = self.connectedPrinterList[index];
        
        [printer disconnect];
    }
}

- (IBAction)printerStatusAction:(UIButton *)sender {
    NSInteger index  = [self.connectedPickerView selectedRowInComponent:0];
    if (self.connectedPrinterList.count > index) {
        TestWIFIConnecter * printer = self.connectedPrinterList[index];
        __weak typeof(self) weakSelf = self;
        
        if (self.mode == 0) {
            [printer printerStatus:^(NSData *status) {
                [weakSelf getStatusWithData:status];
            }];
        } else {
            [printer labelPrinterStatus:^(NSData *status) {
                [weakSelf toastWith:status];
            }];
        }

    }
}

- (IBAction)posTextAction:(UIButton *)sender {
    NSMutableData *dataM =  [[NSMutableData alloc] init];
    NSStringEncoding gbkEncoding = CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000);
    
    if (self.mode == 0) {
        dataM = [NSMutableData dataWithData:[POSCommand initializePrinter]];
        [dataM appendData: [@"中文123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" dataUsingEncoding: gbkEncoding]];
        [dataM appendData:[POSCommand printAndFeedForwardWhitN:6]];
    } else {
        [dataM appendData:[TSCCommand sizeBymmWithWidth:70 andHeight:85]];
        [dataM appendData:[TSCCommand gapBymmWithWidth:2 andHeight:0]];
        [dataM appendData:[TSCCommand cls]];
        [dataM appendData:[TSCCommand textWithX:0 andY:10 andFont:@"2" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"12 x 20 font" usStrEnCoding:gbkEncoding]];
        [dataM appendData:[TSCCommand textWithX:0 andY:80 andFont:@"3" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"16 x 24  font" usStrEnCoding:gbkEncoding]];
        [dataM appendData:[TSCCommand textWithX:0 andY:160 andFont:@"4" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"24 x 32 font" usStrEnCoding:gbkEncoding]];
        [dataM appendData:[TSCCommand textWithX:0 andY:240 andFont:@"5" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"32 x 48 font" usStrEnCoding:gbkEncoding]];
        [dataM appendData:[TSCCommand textWithX:0 andY:320 andFont:@"6" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"14 x 19 font" usStrEnCoding:gbkEncoding]];
        [dataM appendData:[TSCCommand textWithX:0 andY:400 andFont:@"7" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"21 x 27 font" usStrEnCoding:gbkEncoding]];
        [dataM appendData:[TSCCommand textWithX:0 andY:480 andFont:@"8" andRotation:0 andX_mul:1 andY_mul:1 andContent:@"14 x25 font" usStrEnCoding:gbkEncoding]];
        [dataM appendData:[TSCCommand print:1]];
    }
    
    [self printWithData:dataM];
}

- (IBAction)posQRCodeAction:(UIButton *)sender {
    NSMutableData *dataM = [[NSMutableData alloc] init];
    
    if (self.mode == 0) {
        dataM = [NSMutableData dataWithData:[POSCommand initializePrinter]];
        [dataM appendData:[POSCommand selectAlignment:1]];
        [dataM appendData:[POSCommand printQRCode:6 level:48 code:@"www.google.com" useEnCodeing:NSUTF8StringEncoding]];
        [dataM appendData:[POSCommand printAndFeedForwardWhitN:6]];
        [dataM appendData:[POSCommand selectCutPageModelAndCutpage:1]];
    } else {
        [dataM appendData:[TSCCommand sizeBymmWithWidth:80 andHeight:100]];
        [dataM appendData:[TSCCommand gapBymmWithWidth:2 andHeight:0]];
        [dataM appendData:[TSCCommand cls]];
        [dataM appendData:[TSCCommand qrCodeWithX:280 andY:10 andEccLevel:@"M" andCellWidth:8 andMode:@"A" andRotation:0 andContent:@"www.google.com" usStrEnCoding:NSUTF8StringEncoding]];
        [dataM appendData:[TSCCommand print:1]];
    }
    
    [self printWithData:dataM];
}

- (IBAction)posBarCodeAction:(UIButton *)sender {
    NSMutableData *dataM = [[NSMutableData alloc] init];
    
    if (self.mode == 0) {
        dataM = [NSMutableData dataWithData:[POSCommand initializePrinter]];
        [dataM appendData:[POSCommand selectHRICharactersPrintPosition:2]];
        [dataM appendData:[POSCommand selectAlignment:1]];
        [dataM appendData:[POSCommand setBarcodeHeight:60]];
        [dataM appendData:[POSCommand setBarcodeWidth:2]];
        [dataM appendData:[POSCommand printBarcodeWithM:4  andContent:@"ABCDEFGHI" useEnCodeing:NSUTF8StringEncoding]];
        [dataM appendData:[POSCommand printAndFeedForwardWhitN:6]];
        [dataM appendData:[POSCommand selectCutPageModelAndCutpage:1]];
    } else {
        [dataM appendData:[TSCCommand sizeBymmWithWidth:80 andHeight:100]];
        [dataM appendData:[TSCCommand gapBymmWithWidth:2 andHeight:0]];
        [dataM appendData:[TSCCommand cls]];
        [dataM appendData:[TSCCommand barcodeWithX:100 andY:50 andCodeType:@"128" andHeight:80 andHunabReadable:2 andRotation:0 andNarrow:2 andWide:2 andContent:@"12345678" usStrEnCoding:NSUTF8StringEncoding]];
        [dataM appendData:[TSCCommand print:1]];
    }
    
    [self printWithData:dataM];
}

- (IBAction)posPictureAction:(UIButton *)sender {
    UIImage *img = [UIImage imageNamed:@"image"];
    NSMutableData *dataM = [[NSMutableData alloc] init];
    
    if (self.mode == 0) {
        dataM = [NSMutableData dataWithData:[POSCommand initializePrinter]];
        [dataM appendData:[POSCommand selectAlignment:1]];
        [dataM appendData:[POSCommand printRasteBmpWithM:RasterNolmorWH andImage:img andType:Dithering]];
        [dataM appendData:[POSCommand printAndFeedForwardWhitN:6]];
        [dataM appendData:[POSCommand selectCutPageModelAndCutpage:1]];
    } else {
        [dataM appendData:[TSCCommand sizeBymmWithWidth:80 andHeight:50]];
        [dataM appendData:[TSCCommand gapBymmWithWidth:0 andHeight:0]];
        [dataM appendData:[TSCCommand cls]];
        [dataM appendData:[TSCCommand bitmapWithX:0 andY:0 andMode:0 andImage:img]];
        [dataM appendData:[TSCCommand print:1]];
    }

    [self printWithData:dataM];
    
}

- (IBAction)printMultipleCopiesAction:(UIButton *)sender {
    
    UIImage *img = [UIImage imageNamed:@"image"];
    
    NSMutableData *dataM = [[NSMutableData alloc] init];
    
    if (self.mode == 0) {
        dataM = [NSMutableData dataWithData:[POSCommand initializePrinter]];
        [dataM appendData:[POSCommand selectAlignment:1]];
        [dataM appendData:[POSCommand printRasteBmpWithM:RasterNolmorWH andImage:img andType:Dithering]];
        [dataM appendData:[POSCommand printAndFeedForwardWhitN:6]];
        [dataM appendData:[POSCommand selectCutPageModelAndCutpage:1]];
    } else {
        [dataM appendData:[TSCCommand sizeBymmWithWidth:80 andHeight:50]];
        [dataM appendData:[TSCCommand gapBymmWithWidth:0 andHeight:0]];
        [dataM appendData:[TSCCommand cls]];
        [dataM appendData:[TSCCommand bitmapWithX:0 andY:0 andMode:0 andImage:img]];
        [dataM appendData:[TSCCommand print:1]];
    }
    
    for (TestWIFIConnecter *printer in self.connectedPrinterList) {
        [printer writeCommandWithData:dataM];
    }
    
}


// MARK: POSWIFIConnecterDelegate

// connect success
- (void)wifiPOSConnectedToHost:(NSString *)ip port:(UInt16)port mac:(NSString *)mac {
    NSLog(@"printer ip:%@,printer mac:%@ connection succeeded",ip,mac);
    [self.macAddressTF resignFirstResponder];
    [self buttonStatusOn];
    [self.connectedPickerView reloadAllComponents];
    __block NSUInteger index = 0;
    [self.connectedPrinterList enumerateObjectsUsingBlock:^(TestWIFIConnecter * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if (obj.deviceMac == mac) {
            index= idx;
            *stop = YES;
        }
    }];
    
    [self.connectedPickerView selectRow:index inComponent:0 animated:YES];
    
}

// disconnect error
- (void)wifiPOSDisconnectWithError:(NSError *)error mac:(NSString *)mac ip:(NSString *)ip {
    
    for (TestWIFIConnecter *printer in self.connectedPrinterList) {
        if (printer.deviceMac == mac || printer.deviceIP == ip) {
            [self.connectedPrinterList removeObject:printer];
            break;
        }
    }
    
    [self.connectedPickerView reloadAllComponents];
    if (self.connectedPrinterList.count == 0) {
        [self buttonStatusOff];
    }
    
}

// send data success
- (void)wifiPOSWriteValueWithTag:(long)tag mac:(NSString *)mac ip:(NSString *)ip {
    NSLog(@"printer ip :%@,printer mac:%@ write success",ip,mac);
}

// receive printer data
- (void)wifiPOSReceiveValueForData:(NSData *)data mac:(NSString *)mac ip:(NSString *)ip {
    NSLog(@"printer ip :%@,printer mac:%@ receive success",ip,mac);
}


// MARK: UITextFieldDelegate

-(BOOL)textFieldShouldBeginEditing:(UITextField *)textField {
    [self sendFindCmd];
    self.macAddressTableView.hidden = NO;
    return YES;
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    [self sendFindCmd];
    self.macAddressTableView.hidden = NO;
    return YES;
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    self.macAddressTableView.hidden = YES;
    return YES;
}


// MARK: UITableViewDelegate

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.udpList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *cellId = @"printerCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellId];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellId];
    }
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    
    PrinterProfile *profile = self.udpList[indexPath.row];
    cell.textLabel.text = profile.printerName;
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 100;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    if (self.udpList.count) {
        PrinterProfile *profile = self.udpList[indexPath.row];
        self.macAddressTF.text = profile.printerName;
        [self.macAddressTF resignFirstResponder];
        self.macAddressTableView.hidden = YES;
    }
}


// MARK: UIPickerViewDelegate UIPickerViewDatasource

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

// returns the # of rows in each component..
- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    return self.connectedPrinterList.count;
}

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    
    if (self.connectedPrinterList.count > row) {
        TestWIFIConnecter *printer = self.connectedPrinterList[row];
        return (printer.deviceMac)?printer.deviceMac:printer.deviceIP;
    }
    
    return @"";
}

-(CGFloat)pickerView:(UIPickerView *)pickerView rowHeightForComponent:(NSInteger)component {
    return 44;
}


@end
