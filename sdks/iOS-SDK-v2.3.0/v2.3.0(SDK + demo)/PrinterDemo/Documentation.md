###说明文档
Printer是一个demo项目，一个可蓝牙、WiFi连接并打印的demo，可向连接打印机发送数据并接收回传数据。相关SDK在目录~./Printer/PrinterSDK/...

###API说明 
蓝牙管理类 ： [TSCBLEManager.h] [POSBLEManager.h]
			负责蓝牙的连接管理，数据的发送和接收,通过代理回调（详细见 TSCPrinterSDK.h, POSPrinterSDK.h）

WiFi管理类 ： [TSCWIFIanager.h] [POSBLEManager.h]
            负责WiFi的连接管理，数据的发送和接收,通过代理回调（详细见 TSCPrinterSDK.h, POSPrinterSDK.h）

使用SDK需要添加系统依赖库:
                                                    SystemConfiguration.framework
                                                    CoreBluetooth.framework
                                                    CFNetwork.framework

# UDP Broadcast
Using the Multicast Networking Additional Capability
If your app uses UDP broadcast no printer in LAN, you should request
‘Multicast Networking Entitlement’, and setts up it.
link: https://developer.apple.com/contact/request/networking-multicast
