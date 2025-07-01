//
//  TSCBlueListVC.m
//  Printer
//

#import "TSCBlueListVC.h"
#import "TSCBLEManager.h"
#import "UIView+Toast.h"

@interface TSCBlueListVC ()<UITableViewDelegate,UITableViewDataSource, TSCBLEManagerDelegate>

@property (weak, nonatomic) IBOutlet UITableView *myTable;
@property (nonatomic,strong) NSMutableArray *dataArr;
@property (nonatomic,strong) NSMutableArray *rssiList;
@property (strong, nonatomic) UIActivityIndicatorView *indicator;

@end

@implementation TSCBlueListVC

- (void)viewDidLoad {
    [super viewDidLoad];
    self.automaticallyAdjustsScrollViewInsets = NO;
    
    [TSCBLEManager sharedInstance].delegate = self;
    [[TSCBLEManager sharedInstance] startScan];
    
    _indicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
    _indicator.center = self.view.center;
    [self.view addSubview:self.indicator];

}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    [[TSCBLEManager sharedInstance] removeDelegate:self];
}


#pragma mark - TSCBLEManagerDelegate

- (void)TSCbleUpdatePeripheralList:(NSArray *)peripherals RSSIList:(NSArray *)rssiList{
    _dataArr = [NSMutableArray arrayWithArray:peripherals];
    _rssiList = [NSMutableArray arrayWithArray:rssiList];
    [self.myTable reloadData];
}

/** 连接成功 */
- (void)TSCbleConnectPeripheral:(CBPeripheral *)peripheral{
    [self.indicator stopAnimating];
    // 返回主页控制器
    [self.navigationController popViewControllerAnimated:YES];
}

// 连接失败
- (void)TSCbleFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    [self.indicator stopAnimating];
    [self.view makeToast:@"connect fail" duration:1.f position:CSToastPositionCenter];
}

#pragma mark - 表数据源 代理

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
    [[TSCBLEManager sharedInstance] connectDevice:peripheral];
}

- (IBAction)scanAgain:(id)sender {
    [self.dataArr removeAllObjects];
    [[TSCBLEManager sharedInstance] startScan];
}

#pragma mark - lazy
- (NSMutableArray *)dataArr {
    if (!_dataArr) {
        _dataArr = [NSMutableArray array];
    }
    return _dataArr;
}

@end
