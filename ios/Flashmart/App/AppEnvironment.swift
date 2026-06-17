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
    let mainViewModel: MainViewModel
    let razorpayCoordinator: RazorpayPaymentCoordinator

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
        self.mainViewModel = MainViewModel(apiClient: api, sessionStore: session, cartStore: cart, localDemoStore: demo)
        self.razorpayCoordinator = RazorpayPaymentCoordinator()
        session.hydrate()
    }
}
