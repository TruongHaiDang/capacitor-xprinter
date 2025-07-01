//
//  POSBlueListVC.m
//  Printer
//

#import "POSBlueListVC.h"
#import "POSBLEManager.h"
#import "UIView+Toast.h"

@interface POSBlueListVC ()<UITableViewDelegate, UITableViewDataSource, POSBLEManagerDelegate>

@property (weak, nonatomic) IBOutlet UITableView *myTable;
@property (strong, nonatomic) NSMutableArray *dataArr;
@property (strong, nonatomic) NSMutableArray *rssiList;
@property (strong, nonatomic) UIActivityIndicatorView *indicator;

@end

@implementation POSBlueListVC

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.automaticallyAdjustsScrollViewInsets = NO;
    
    [POSBLEManager sharedInstance].delegate = self;
    [[POSBLEManager sharedInstance] startScan];
    
    _indicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
    _indicator.center = self.view.center;
    [self.view addSubview:self.indicator];
}

- (void)dealloc {
    [[POSBLEManager sharedInstance] removeDelegate:self];
}


#pragma mark - POSBLEManagerDelegate

- (void)POSbleUpdatePeripheralList:(NSArray *)peripherals RSSIList:(NSArray *)rssiList {
    _dataArr = [NSMutableArray arrayWithArray:peripherals];
    _rssiList = [NSMutableArray arrayWithArray:rssiList];
    [self.myTable reloadData];
}

- (void)POSbleConnectPeripheral:(CBPeripheral *)peripheral {
    [self.indicator stopAnimating];
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)POSbleFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    [self.indicator stopAnimating];
    [self.view makeToast:@"connect fail" duration:1.f position:CSToastPositionCenter];
}


#pragma mark - UITableViewDelegate

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataArr.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *cellId = @"printerCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellId];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellId];
    }
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    
    if (indexPath.row < self.dataArr.count) {
        CBPeripheral *peripheral = self.dataArr[indexPath.row];
        cell.textLabel.text = peripheral.name;
        if (peripheral.name.length == 0) {
            cell.textLabel.text = @"unknow";
        }
    }
    
    if (indexPath.row < self.rssiList.count) {
        NSNumber *rssi = self.rssiList[indexPath.row];
        cell.detailTextLabel.text = [NSString stringWithFormat:@"RSSI:%zd",rssi.integerValue];
    }
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 100;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    CBPeripheral *peripheral = self.dataArr[indexPath.row];
    [self.indicator startAnimating];

    [[POSBLEManager sharedInstance] connectDevice:peripheral];
}

- (IBAction)scanAgain:(id)sender {
    [self.dataArr removeAllObjects];
    [[POSBLEManager sharedInstance] startScan];
}

#pragma mark - lazy
- (NSMutableArray *)dataArr {
    if (!_dataArr) {
        _dataArr = [NSMutableArray array];
    }
    return _dataArr;
}

@end
