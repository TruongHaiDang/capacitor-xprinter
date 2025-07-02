//
//  POSUdpListVC.m
//  Printer
//

#import "POSUdpListVC.h"
#import "POSIPConfigVC.h"

#import "POSWIFIManager.h"

@interface POSUdpListVC ()<UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (strong, nonatomic) NSMutableArray *printerList;

@end

@implementation POSUdpListVC

- (void)viewDidLoad {
    [super viewDidLoad];
    [self searchPrinter];
}

- (void)dealloc {
    [[POSWIFIManager sharedInstance] closeUdpSocket];
}

- (void)searchPrinter {
    if ([[POSWIFIManager sharedInstance] createUdpSocket]) {
        
        if (self.printerList.count > 0) {
            [self.printerList removeAllObjects];
        }
        
        __weak typeof(self) weakSelf = self;
        [[POSWIFIManager sharedInstance] sendFindCmd:^(PrinterProfile *printer) {
            
            for (int i = 0; i < self.printerList.count; i++) {
                PrinterProfile *profile = weakSelf.printerList[i];
                if ([profile.printerName isEqualToString:printer.printerName]) {
                    return;
                }
            }
            
            [weakSelf.printerList addObject:printer];
            [weakSelf.tableView reloadData];
            
        }];
    }
}

- (IBAction)rescanAction:(UIBarButtonItem *)sender {
    [[POSWIFIManager sharedInstance] closeUdpSocket];
    [self searchPrinter];
}


#pragma mark - UITableViewDelegate

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 80.f;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.printerList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *cellId = @"printerCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellId];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellId];
    }
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    
    PrinterProfile *printer = self.printerList[indexPath.row];
    cell.textLabel.text = printer.printerName;
    cell.detailTextLabel.text = printer.printerDesc;
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    PrinterProfile *printer = self.printerList[indexPath.row];
    
    [POSWIFIManager sharedInstance].connectedPrinter = printer;
    
    POSIPConfigVC *configVC = [POSIPConfigVC initControllerWith:printer];
    [self.navigationController pushViewController:configVC animated:YES];
}


#pragma mark - Lazy

- (NSMutableArray *)printerList {
    if (!_printerList) {
        _printerList = [[NSMutableArray alloc] init];
    }
    return _printerList;
}

@end
