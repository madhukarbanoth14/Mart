import Foundation
import Observation

@MainActor
@Observable
final class AppEnvironment {
    let sessionStore: SessionStore
    let cartStore: CartStore
    let localDemoStore: LocalDemoStore
    let apiClient: MartAPIClient
    let authViewModel: AuthViewModel
    let registerViewModel: RegisterViewModel
    let mainViewModel: MainViewModel
    let financeViewModel: FinanceViewModel
    let returnsViewModel: ReturnsViewModel
    let razorpayCoordinator: RazorpayPaymentCoordinator
    let pushTokenRegistrar: PushTokenRegistrar

    init() {
        let session = SessionStore()
        let demo = LocalDemoStore()
        let cart = CartStore()
        let api = MartAPIClient(sessionStore: session, localDemoStore: demo)
        self.sessionStore = session
        self.cartStore = cart
        self.localDemoStore = demo
        self.apiClient = api
        self.authViewModel = AuthViewModel(sessionStore: session, apiClient: api, localDemoStore: demo)
        self.registerViewModel = RegisterViewModel(apiClient: api, sessionStore: session)
        self.mainViewModel = MainViewModel(apiClient: api, sessionStore: session, cartStore: cart, localDemoStore: demo)
        self.financeViewModel = FinanceViewModel(apiClient: api)
        self.returnsViewModel = ReturnsViewModel(apiClient: api)
        self.razorpayCoordinator = RazorpayPaymentCoordinator()
        self.pushTokenRegistrar = PushTokenRegistrar(apiClient: api, sessionStore: session)
        session.hydrate()
    }
}
