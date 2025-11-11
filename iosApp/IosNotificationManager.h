#import <Foundation/Foundation.h>

@interface IosNotificationManager : NSObject
- (void)requestPermission:(void (^)(BOOL granted))completion;
- (void)showNotificationWithTitle:(NSString *)title message:(NSString *)message;
@end