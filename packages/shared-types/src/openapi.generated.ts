/* eslint-disable */
// Généré par scripts/contract.mjs depuis docs/api/openapi.json — ne pas modifier à la main.

export interface paths {
    "/api/admin/catalog": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["managedCatalog"];
        put?: never;
        post: operations["create_3"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/admin/catalog/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["update"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/admin/catalog/{id}/active": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch: operations["setActive_1"];
        trace?: never;
    };
    "/api/auth/logout": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["logout_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/auth/me": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["me_2"];
        put: operations["updateMe_2"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/auth/otp/request": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["requestOtp"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/auth/otp/verify": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["verifyOtp"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/auth/permissions": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["permissions_1"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/auth/refresh": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["refresh_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/catalog": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["publicCatalog_1"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/health": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["health"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/audit": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["audit"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/beneficiaries": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["beneficiaries"];
        put?: never;
        post: operations["createBeneficiary"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/beneficiaries/{id}/archive": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["archive"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/catalog": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["managedCatalog_1"];
        put?: never;
        post: operations["create_4"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/catalog/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["update_1"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/catalog/{id}/active": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch: operations["setActive"];
        trace?: never;
    };
    "/api/v1/admin/catalog/targets": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["all"];
        put?: never;
        post: operations["create_2"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/catalog/targets/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["update_2"];
        post?: never;
        delete: operations["delete"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/contact-messages": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["contactMessages"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/contributions/{id}/decision": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["decideContribution"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/dashboard": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["dashboard_1"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/disputes": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["disputes"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/disputes/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["updateDispute"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/finances": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["finances"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/installments/{id}/done": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["installmentDone"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/listings": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["adminList_1"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/listings/{id}/moderate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["moderate"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/missions": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["missions"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/missions/{id}/cancel": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["cancelMission"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/missions/{id}/reassign": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["reassign"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/missions/{id}/validate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["validate"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/payment-transactions": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["recent"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/payments": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["payments"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/payments/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["updatePayment"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/payments/{id}/payout": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["payout"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/proofs": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["proofs"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/proofs/{id}/review": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["reviewProof"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/providers": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["adminList"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/providers/{id}/verified": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch: operations["verify"];
        trace?: never;
    };
    "/api/v1/admin/providers/directory": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["directory"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/providers/options": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["options_1"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/requests": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["requests"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/requests/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["request_1"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/requests/{id}/beneficiary": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["linkBeneficiary"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/requests/{id}/close": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["close"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/requests/{id}/complete": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["complete_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/requests/{id}/decision": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["decide_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/requests/{id}/finance": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["finance"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/requests/{id}/missions": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["createMission"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/requests/target-usage": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["targetUsage"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/rewards": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["overview"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/rewards/accounts": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["accounts"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/rewards/adjust": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["adjust"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/rewards/rules": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["createRule"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/rewards/rules/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["saveRule"];
        post?: never;
        delete: operations["deleteRule"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/rewards/transactions/{id}/decision": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["decide"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/settings": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["allSettings"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/settings/{key}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["saveSetting"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/sms": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["sms"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/stats": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["stats"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/stats/export": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["export"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/users": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["list"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/users/{id}/role": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["setRole"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/users/{id}/status": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch: operations["setStatus"];
        trace?: never;
    };
    "/api/v1/admin/users/options": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["options"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/logout": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["logout"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/me": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["me_1"];
        put: operations["updateMe_1"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/otp/request": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["requestOtp_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/otp/verify": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["verifyOtp_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/permissions": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["permissions"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/refresh": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["refresh"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/beneficiary/requests": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["received"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/cart": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["cart"];
        put?: never;
        post: operations["addToCart"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/cart/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete: operations["removeCartItem"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/cart/checkout": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["checkout"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/catalog": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["publicCatalog"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/catalog/targets": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["active"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/contact": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["contact"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/disputes/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["dispute"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/disputes/{id}/messages": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["disputeMessage"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/files/{kind}/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["file"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/listings": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["create_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/listings/{id}/contributions": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["contribute"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/listings/{id}/mena": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["supportMena"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/listings/dashboard": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["dashboard"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/listings/mine": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["mine_1"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/listings/published": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["published"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/missions/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["mission"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/missions/{id}/complete": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["complete"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/missions/{id}/respond": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["respond"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/missions/{id}/start": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["start"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/missions/dashboard": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["providerDashboard"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/missions/mine": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["myMissions"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/notifications": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["notifications"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/notifications/read-all": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["readAll"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/notifications/unread-count": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["unread"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/payments/{id}/declare": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["declare"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/payments/{id}/pay": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["pay"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/payments/{id}/transactions": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["transactions"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/payments/mine": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["myPayments"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/payments/online/config": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["config"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/payments/transactions/{txId}/simulate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["simulate"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/payments/webhook": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["webhook"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/proofs": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["uploadProof"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/providers/me": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["me"];
        put: operations["updateMe"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/providers/me/services": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["addService"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/providers/me/services/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete: operations["removeService"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/providers/search": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["search"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/public/listings": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["publicListings"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/public/partners": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["partners"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/public/providers": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["publicProviders"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/reference": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["reference"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/requests": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["myRequests"];
        put?: never;
        post: operations["createCustom"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/requests/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["request"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/requests/{id}/answer": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["answer"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/requests/{id}/cancel": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["cancel"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/requests/{id}/disputes": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["openDispute"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/requests/{id}/review": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["review"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/requests/dashboard": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["donorDashboard"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/rewards/conversion": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["conversion"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/rewards/me": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["me_3"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/rewards/transfer": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["transfer"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/services/orders": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["mine"];
        put?: never;
        post: operations["create"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/services/orders/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["detail"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/services/orders/{id}/messages": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["message"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/services/orders/{id}/transition": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["transition"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/settings/public": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["publicSettings"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
}
export type webhooks = Record<string, never>;
export interface components {
    schemas: {
        AccountView: {
            /** Format: int32 */
            balance: number;
            /** Format: int32 */
            earned: number;
            /** Format: int64 */
            userId: number | null;
        };
        AddToCart: {
            /** Format: int64 */
            catalogItemId: number;
            /** Format: int32 */
            quantity: number;
            target: string | null;
        };
        AdjustRequest: {
            /** Format: int32 */
            amount: number;
            reason: string | null;
            /** Format: int64 */
            userId: number | null;
        };
        AdminDashboard: {
            /** Format: int64 */
            collected: number;
            /** Format: int64 */
            completed: number;
            /** Format: int64 */
            infoRequested: number;
            /** Format: int64 */
            lateMissions: number;
            /** Format: int64 */
            openDisputes: number;
            /** Format: int64 */
            paymentsToFollow: number;
            /** Format: int64 */
            proofsToReview: number;
            recent: components["schemas"]["RequestRow"][];
            /** Format: int64 */
            toTreat: number;
            /** Format: int64 */
            users: number;
        };
        AdminDispute: {
            dispute: components["schemas"]["DisputeRow"] | null;
            openedByName: string | null;
        };
        AdminListings: {
            listings: components["schemas"]["ListingView"][];
            pendingContributions: components["schemas"]["ContributionView"][];
        };
        AdminMission: {
            mission: components["schemas"]["MissionRow"] | null;
            providerName: string | null;
            requestRef: string | null;
        };
        AdminMissions: {
            missions: components["schemas"]["AdminMission"][];
            providers: components["schemas"]["ProviderOption"][];
        };
        AdminOverview: {
            /** Format: int64 */
            circulating: number;
            events: components["schemas"]["EventOption"][];
            /** Format: int32 */
            manualAdjustMax: number;
            rules: components["schemas"]["Rule"][];
            transactions: components["schemas"]["AdminTransaction"][];
        };
        AdminPayment: {
            payment: components["schemas"]["PaymentRow"] | null;
            userName: string | null;
            userPhone: string | null;
        };
        AdminPayments: {
            /** Format: int64 */
            paid: number;
            payments: components["schemas"]["AdminPayment"][];
            /** Format: int64 */
            planned: number;
        };
        AdminProof: {
            proof: components["schemas"]["ProofRow"] | null;
            requestRef: string | null;
            uploaderName: string | null;
        };
        AdminRequestDetail: {
            beneficiaries: components["schemas"]["BeneficiaryRow"][];
            beneficiary: components["schemas"]["BeneficiaryRow"] | null;
            checks: components["schemas"]["Check"][];
            disputes: components["schemas"]["DisputeRow"][];
            donorName: string | null;
            donorPhone: string | null;
            history: components["schemas"]["HistoryRow"][];
            installments: components["schemas"]["InstallmentRow"][];
            items: components["schemas"]["ItemRow"][];
            margin: components["schemas"]["Margin"] | null;
            missions: components["schemas"]["AdminMission"][];
            payments: components["schemas"]["PaymentRow"][];
            proofs: components["schemas"]["ProofRow"][];
            providers: components["schemas"]["ProviderOption"][];
            request: components["schemas"]["RequestRow"] | null;
            /** Format: int32 */
            targetMarginPct: number;
        };
        AdminRequestRow: {
            donorName: string | null;
            donorPhone: string | null;
            request: components["schemas"]["RequestRow"] | null;
        };
        AdminTransaction: {
            /** Format: int32 */
            amount: number;
            /** Format: date-time */
            createdAt: string | null;
            /** Format: int64 */
            createdBy: number | null;
            /** Format: int64 */
            id: number | null;
            reason: string | null;
            status: string | null;
            type: string | null;
            /** Format: int64 */
            userId: number | null;
            userName: string | null;
        };
        AdminUserView: {
            /** Format: date-time */
            createdAt: string | null;
            /** Format: int64 */
            id: number | null;
            /** Format: date-time */
            lastLoginAt: string | null;
            name: string | null;
            organization: string | null;
            permissions: string[];
            phone: string | null;
            role: string | null;
            status: string | null;
            zone: string | null;
        };
        Annex: {
            officiant: boolean;
            other: string | null;
            packaging: boolean;
            packagingNotes: string | null;
            photos: boolean;
            video: boolean;
        };
        Answer: {
            answer: string | null;
        };
        Archive: {
            restore: boolean;
        };
        AuditView: {
            action: string | null;
            /** Format: int64 */
            actorId: number | null;
            actorName: string | null;
            actorRole: string | null;
            /** Format: date-time */
            createdAt: string | null;
            details: string | null;
            entity: string | null;
            entityId: string | null;
            /** Format: int64 */
            id: number | null;
            ip: string | null;
        };
        BadgeView: {
            code: string | null;
            desc: string | null;
            earned: boolean;
            emoji: string | null;
            label: string | null;
        };
        BeneficiaryContact: {
            address: string | null;
            fullName: string | null;
            phone: string | null;
            zone: string | null;
        };
        BeneficiaryRequest: {
            address: string | null;
            consent: boolean;
            fullName: string | null;
            needs: string | null;
            phone: string | null;
            /** Format: int64 */
            userId: number | null;
            zone: string | null;
        };
        BeneficiaryRow: {
            address: string | null;
            /** Format: date-time */
            archivedAt: string | null;
            consent: boolean;
            /** Format: date-time */
            createdAt: string | null;
            /** Format: int64 */
            createdBy: number | null;
            fullName: string | null;
            /** Format: int64 */
            id: number | null;
            needs: string | null;
            notes: string | null;
            phone: string | null;
            /** Format: int64 */
            userId: number | null;
            zone: string | null;
        };
        Cart: {
            annexPrices: {
                [key: string]: number;
            };
            lines: components["schemas"]["CartLine"][];
            /** Format: int32 */
            total: number;
        };
        CartLine: {
            /** Format: int64 */
            catalogItemId: number | null;
            category: string | null;
            emoji: string | null;
            /** Format: int64 */
            id: number | null;
            /** Format: int32 */
            quantity: number;
            target: string | null;
            title: string | null;
            /** Format: int32 */
            unitPrice: number;
        };
        Catalog: {
            labels: {
                [key: string]: {
                    [key: string]: string;
                };
            };
            selfRegisterRoles: string[];
            statuses: {
                [key: string]: {
                    [key: string]: components["schemas"]["Status"];
                };
            };
        };
        CatalogItemResponse: {
            category: string | null;
            description: string | null;
            emoji: string | null;
            /** Format: int64 */
            id: number | null;
            kind: string | null;
            slug: string | null;
            title: string | null;
            unit: string | null;
            /** Format: int32 */
            unitPrice: number;
        };
        CatalogResponse: {
            currency: string | null;
            items: components["schemas"]["CatalogItemResponse"][];
        };
        Check: {
            help: string | null;
            label: string | null;
            ok: boolean;
        };
        CheckoutRequest: {
            annex: components["schemas"]["Annex"] | null;
            description: string | null;
            idempotencyKey: string | null;
            schedule: components["schemas"]["Schedule"] | null;
            targetName: string | null;
            targetZone: string | null;
        };
        ContactRequest: {
            email: string | null;
            message: string | null;
            name: string | null;
            phone: string | null;
            subject: string | null;
        };
        ContactView: {
            /** Format: date-time */
            createdAt: string | null;
            email: string | null;
            handled: boolean;
            /** Format: int64 */
            id: number | null;
            message: string | null;
            name: string | null;
            phone: string | null;
            subject: string | null;
        };
        Contribute: {
            /** Format: int32 */
            amount: number;
            method: string | null;
            reference: string | null;
        };
        Contribution: {
            /** Format: int32 */
            amount: number;
            /** Format: date-time */
            createdAt: string | null;
            /** Format: int64 */
            id: number | null;
            /** Format: int64 */
            listingId: number | null;
            method: string | null;
            reference: string | null;
            status: string | null;
            /** Format: int64 */
            userId: number | null;
        };
        ContributionDecision: {
            decision: string | null;
        };
        ContributionView: {
            contribution: components["schemas"]["Contribution"] | null;
            listingTitle: string | null;
            userName: string | null;
        };
        ConversionRequest: {
            /** Format: int32 */
            amount: number;
        };
        Created: {
            /** Format: int64 */
            id: number | null;
            ref: string | null;
        };
        CreateListing: {
            description: string | null;
            endsAt: string | null;
            /** Format: int32 */
            goalAmount: number;
            title: string | null;
            type: string | null;
            zone: string | null;
        };
        CreateMission: {
            compensationMode: string | null;
            dueDate: string | null;
            exceptionReason: string | null;
            exchangeDescription: string | null;
            instructions: string | null;
            /** Format: int32 */
            providerFee: number;
            /** Format: int64 */
            providerId: number | null;
            title: string | null;
        };
        CreateOrder: {
            compensationMode: string | null;
            description: string | null;
            exchangeDescription: string | null;
            /** Format: int32 */
            price: number;
            /** Format: int64 */
            providerId: number | null;
            title: string | null;
            zone: string | null;
        };
        CustomRequest: {
            /** Format: int32 */
            amount: number;
            annex: components["schemas"]["Annex"] | null;
            description: string | null;
            idempotencyKey: string | null;
            kind: string | null;
            schedule: components["schemas"]["Schedule"] | null;
            targetName: string | null;
            targetZone: string | null;
            title: string | null;
        };
        Decision: {
            decision: string | null;
            message: string | null;
            /** Format: int32 */
            newAmount: number | null;
        };
        DecisionRequest: {
            approve: boolean;
        };
        DisputeDetail: {
            againstName: string | null;
            dispute: components["schemas"]["DisputeRow"] | null;
            messages: components["schemas"]["DisputeMessageView"][];
            openedByName: string | null;
            responsibleName: string | null;
        };
        DisputeMessageRow: {
            attachmentName: string | null;
            /** Format: int64 */
            authorId: number | null;
            body: string | null;
            /** Format: date-time */
            createdAt: string | null;
            /** Format: int64 */
            disputeId: number | null;
            /** Format: int64 */
            id: number | null;
        };
        DisputeMessageView: {
            authorName: string | null;
            message: components["schemas"]["DisputeMessageRow"] | null;
        };
        DisputeRequest: {
            description: string | null;
            subject: string | null;
        };
        DisputeRow: {
            /** Format: int64 */
            againstUserId: number | null;
            /** Format: date-time */
            closedAt: string | null;
            /** Format: date-time */
            createdAt: string | null;
            decision: string | null;
            description: string | null;
            /** Format: int64 */
            id: number | null;
            /** Format: int64 */
            missionId: number | null;
            /** Format: int64 */
            openedBy: number | null;
            ref: string | null;
            /** Format: int64 */
            requestId: number | null;
            /** Format: int64 */
            responsibleId: number | null;
            status: string | null;
            subject: string | null;
        };
        DisputeUpdate: {
            decision: string | null;
            /** Format: int64 */
            responsibleId: number | null;
            status: string | null;
        };
        DonorDashboard: {
            /** Format: int64 */
            done: number;
            /** Format: int64 */
            ongoing: number;
            /** Format: int64 */
            pending: number;
            /** Format: int64 */
            proofsAvailable: number;
            recent: components["schemas"]["RequestRow"][];
        };
        DonorRequestDetail: {
            canReview: boolean;
            disputes: components["schemas"]["DisputeRow"][];
            history: components["schemas"]["HistoryRow"][];
            installments: components["schemas"]["InstallmentRow"][];
            items: components["schemas"]["ItemRow"][];
            missions: components["schemas"]["MissionSummary"][];
            payments: components["schemas"]["PaymentRow"][];
            proofs: components["schemas"]["ProofRow"][];
            request: components["schemas"]["RequestRow"] | null;
        };
        EventOption: {
            base: boolean;
            key: string | null;
            label: string | null;
            who: string | null;
        };
        Finance: {
            /** Format: int32 */
            executionBudget: number;
            /** Format: int32 */
            otherFees: number;
        };
        FinanceRow: {
            /** Format: int32 */
            actors: number;
            /** Format: int32 */
            executionBudget: number;
            /** Format: int64 */
            id: number | null;
            margin: components["schemas"]["Margin"] | null;
            /** Format: int32 */
            otherFees: number;
            /** Format: int64 */
            partnerId: number | null;
            ref: string | null;
            title: string | null;
            /** Format: int32 */
            totalAmount: number;
        };
        Finances: {
            /** Format: int64 */
            cost: number;
            /** Format: int64 */
            gross: number;
            /** Format: int64 */
            revenue: number;
            rows: components["schemas"]["FinanceRow"][];
            /** Format: int32 */
            targetMarginPct: number;
        };
        HistoryRow: {
            /** Format: int64 */
            actorId: number | null;
            /** Format: date-time */
            createdAt: string | null;
            /** Format: int64 */
            id: number | null;
            note: string | null;
            /** Format: int64 */
            requestId: number | null;
            status: string | null;
        };
        IdentityUserResponse: {
            bio: string | null;
            country: string | null;
            /** Format: date-time */
            createdAt: string | null;
            email: string | null;
            /** Format: int64 */
            id: number | null;
            name: string | null;
            notifPrefs: components["schemas"]["NotificationPreferences"] | null;
            organization: string | null;
            permissions: string[];
            phone: string | null;
            role: string | null;
            status: string | null;
            zone: string | null;
        };
        InstallmentRow: {
            /** Format: int32 */
            amount: number;
            /** Format: date-time */
            dueDate: string | null;
            /** Format: int64 */
            id: number | null;
            /** Format: date-time */
            notifiedAt: string | null;
            /** Format: int64 */
            requestId: number | null;
            /** Format: int32 */
            seq: number;
            status: string | null;
        };
        ItemRow: {
            /** Format: int64 */
            catalogItemId: number | null;
            /** Format: int32 */
            costEstimate: number;
            /** Format: int64 */
            id: number | null;
            label: string | null;
            /** Format: int32 */
            quantity: number;
            /** Format: int64 */
            requestId: number | null;
            target: string | null;
            /** Format: int32 */
            unitPrice: number;
        };
        JsonNode: Record<string, never>;
        Kpi: {
            /** Format: double */
            acceptanceRate: number;
            /** Format: int64 */
            accepted: number;
            /** Format: int64 */
            amount: number;
            /** Format: double */
            avgDays: number;
            /** Format: int64 */
            documented: number;
            /** Format: int64 */
            late: number;
            /** Format: double */
            realizationRate: number;
            /** Format: int64 */
            realized: number;
            /** Format: int64 */
            received: number;
            /** Format: int64 */
            rejected: number;
        };
        Level: {
            key: string | null;
            label: string | null;
            /** Format: int32 */
            min: number;
        };
        LinkBeneficiary: {
            /** Format: int64 */
            beneficiaryId: number | null;
        };
        Listing: {
            /** Format: date-time */
            createdAt: string | null;
            description: string | null;
            /** Format: date-time */
            endsAt: string | null;
            /** Format: int32 */
            goalAmount: number;
            /** Format: int64 */
            id: number | null;
            /** Format: int32 */
            menaSupport: number;
            /** Format: int64 */
            ownerId: number | null;
            /** Format: int32 */
            raisedAmount: number;
            rejectionReason: string | null;
            status: string | null;
            title: string | null;
            type: string | null;
            zone: string | null;
        };
        ListingView: {
            /** Format: int64 */
            confirmedContributions: number;
            listing: components["schemas"]["Listing"] | null;
            organization: string | null;
            ownerName: string | null;
        };
        ManagedCatalogItemResponse: {
            active: boolean;
            category: string | null;
            /** Format: int32 */
            costEstimate: number;
            /** Format: date-time */
            createdAt: string | null;
            description: string | null;
            emoji: string | null;
            /** Format: int64 */
            id: number | null;
            kind: string | null;
            slug: string | null;
            title: string | null;
            unit: string | null;
            /** Format: int32 */
            unitPrice: number;
        };
        Margin: {
            /** Format: int32 */
            cost: number;
            /** Format: int32 */
            fees: number;
            /** Format: int32 */
            gross: number;
            /** Format: double */
            pct: number;
        };
        MissionRequestInfo: {
            annexServices: Record<string, never> | null;
            /** Format: int64 */
            id: number | null;
            ref: string | null;
            targetName: string | null;
            targetZone: string | null;
            title: string | null;
        };
        MissionRow: {
            compensationMode: string | null;
            /** Format: date-time */
            completedAt: string | null;
            /** Format: date-time */
            createdAt: string | null;
            /** Format: int64 */
            createdBy: number | null;
            /** Format: date-time */
            dueDate: string | null;
            exceptionReason: string | null;
            exchangeDescription: string | null;
            exchangeStatus: string | null;
            /** Format: int64 */
            id: number | null;
            instructions: string | null;
            late: boolean;
            /** Format: int32 */
            providerFee: number;
            /** Format: int64 */
            providerId: number | null;
            /** Format: int64 */
            reassignedFrom: number | null;
            refusalReason: string | null;
            /** Format: int64 */
            requestId: number | null;
            status: string | null;
            title: string | null;
            /** Format: date-time */
            updatedAt: string | null;
            /** Format: date-time */
            validatedAt: string | null;
        };
        MissionSummary: {
            /** Format: date-time */
            dueDate: string | null;
            /** Format: int64 */
            id: number | null;
            providerName: string | null;
            status: string | null;
            title: string | null;
            /** Format: date-time */
            validatedAt: string | null;
        };
        Moderate: {
            decision: string | null;
            reason: string | null;
        };
        MyProvider: {
            profile: components["schemas"]["Profile"] | null;
            /** Format: int64 */
            reviewCount: number;
            reviews: components["schemas"]["Review"][];
            services: components["schemas"]["Service"][];
        };
        MyRewards: {
            badges: components["schemas"]["BadgeView"][];
            /** Format: int32 */
            balance: number;
            conversionEnabled: boolean;
            /** Format: int32 */
            earned: number;
            level: components["schemas"]["Level"] | null;
            levels: components["schemas"]["Level"][];
            next: components["schemas"]["Level"] | null;
            transactions: components["schemas"]["TransactionView"][];
            transferEnabled: boolean;
        };
        NotificationPreferences: {
            email: boolean;
            inapp: boolean;
            sms: boolean;
        };
        NotificationView: {
            body: string | null;
            /** Format: date-time */
            createdAt: string | null;
            /** Format: int64 */
            id: number | null;
            link: string | null;
            /** Format: date-time */
            readAt: string | null;
            title: string | null;
            type: string | null;
        };
        OnlinePaymentConfig: {
            operators: string[];
            provider: string | null;
            simulated: boolean;
        };
        Order: {
            /** Format: int32 */
            agreedPrice: number | null;
            /** Format: int64 */
            clientId: number | null;
            compensationMode: string | null;
            /** Format: date-time */
            createdAt: string | null;
            description: string | null;
            exchangeDescription: string | null;
            /** Format: int64 */
            id: number | null;
            /** Format: int64 */
            offerId: number | null;
            /** Format: int32 */
            proposedPrice: number;
            /** Format: int64 */
            providerId: number | null;
            status: string | null;
            title: string | null;
            /** Format: date-time */
            updatedAt: string | null;
            zone: string | null;
        };
        OrderDetail: {
            clientName: string | null;
            iAmProvider: boolean;
            messages: components["schemas"]["ServiceMessageView"][];
            order: components["schemas"]["Order"] | null;
            payments: components["schemas"]["ServicePayment"][];
            providerName: string | null;
        };
        OrderView: {
            clientName: string | null;
            iAmProvider: boolean;
            order: components["schemas"]["Order"] | null;
            providerName: string | null;
        };
        OtpRequest: {
            mode: string | null;
            name: string | null;
            phone: string | null;
            role: string | null;
        };
        OtpResponse: {
            devCode: string | null;
            error: string | null;
            ok: boolean;
        };
        OtpVerification: {
            code: string | null;
            phone: string | null;
        };
        PartnerDashboard: {
            /** Format: int64 */
            pending: number;
            /** Format: int64 */
            published: number;
            /** Format: int64 */
            raised: number;
            /** Format: int64 */
            total: number;
        };
        PaymentRow: {
            /** Format: int32 */
            amountPaid: number;
            /** Format: int32 */
            amountPlanned: number;
            /** Format: int32 */
            amountValidated: number | null;
            /** Format: date-time */
            createdAt: string | null;
            direction: string | null;
            /** Format: date-time */
            dueDate: string | null;
            exchangeDescription: string | null;
            exchangeStatus: string | null;
            hasReceipt: boolean;
            /** Format: int64 */
            id: number | null;
            /** Format: int64 */
            installmentId: number | null;
            kind: string | null;
            method: string | null;
            /** Format: int64 */
            missionId: number | null;
            mode: string | null;
            note: string | null;
            /** Format: date-time */
            paidAt: string | null;
            receiptName: string | null;
            reference: string | null;
            /** Format: int32 */
            remaining: number;
            /** Format: int64 */
            requestId: number | null;
            /** Format: int64 */
            serviceOrderId: number | null;
            status: string | null;
            /** Format: int64 */
            userId: number | null;
        };
        PaymentTransaction: {
            /** Format: int32 */
            amount: number;
            /** Format: date-time */
            createdAt: string | null;
            direction: string | null;
            failureReason: string | null;
            /** Format: int64 */
            id: number | null;
            operator: string | null;
            /** Format: int64 */
            paymentId: number | null;
            phone: string | null;
            provider: string | null;
            providerRef: string | null;
            /** Format: date-time */
            settledAt: string | null;
            status: string | null;
        };
        PaymentTransactionView: {
            message: string | null;
            simulated: boolean;
            transaction: components["schemas"]["PaymentTransaction"] | null;
        };
        PayRequest: {
            operator: string | null;
            phone: string | null;
        };
        PostMessage: {
            body: string | null;
            /** Format: int32 */
            price: number | null;
        };
        Profile: {
            available: boolean;
            category: string | null;
            description: string | null;
            /** Format: int64 */
            userId: number | null;
            verified: boolean;
            zone: string | null;
        };
        ProfileRequest: {
            available: boolean;
            category: string | null;
            description: string | null;
            zone: string | null;
        };
        ProfileUpdate: {
            bio: string | null;
            email: string | null;
            name: string | null;
            notifEmail: boolean;
            notifInapp: boolean;
            notifSms: boolean;
            organization: string | null;
            zone: string | null;
        };
        ProofReview: {
            decision: string | null;
            hide: boolean;
            reason: string | null;
        };
        ProofRow: {
            caption: string | null;
            /** Format: date-time */
            createdAt: string | null;
            fileName: string | null;
            /** Format: int64 */
            id: number | null;
            mime: string | null;
            /** Format: int64 */
            missionId: number | null;
            rejectionReason: string | null;
            /** Format: int64 */
            requestId: number | null;
            /** Format: date-time */
            reviewedAt: string | null;
            /** Format: int64 */
            reviewedBy: number | null;
            /** Format: int64 */
            size: number;
            status: string | null;
            type: string | null;
            /** Format: int64 */
            uploadedBy: number | null;
            visibleToDonor: boolean;
        };
        ProviderCard: {
            available: boolean;
            category: string | null;
            description: string | null;
            /** Format: int64 */
            id: number | null;
            name: string | null;
            phone: string | null;
            /** Format: double */
            rating: number;
            /** Format: int64 */
            reviews: number;
            services: components["schemas"]["Service"][];
            verified: boolean;
            zone: string | null;
        };
        ProviderDashboard: {
            /** Format: int64 */
            accepted: number;
            /** Format: int64 */
            completed: number;
            /** Format: int64 */
            inProgress: number;
            proposals: components["schemas"]["MissionRow"][];
            /** Format: int64 */
            proposed: number;
            /** Format: int64 */
            validated: number;
        };
        ProviderMissionDetail: {
            beneficiary: components["schemas"]["BeneficiaryContact"] | null;
            items: components["schemas"]["ItemRow"][];
            mission: components["schemas"]["MissionRow"] | null;
            proofs: components["schemas"]["ProofRow"][];
            request: components["schemas"]["MissionRequestInfo"] | null;
        };
        ProviderOption: {
            category: string | null;
            /** Format: int64 */
            id: number | null;
            name: string | null;
        };
        PublicListing: {
            description: string | null;
            /** Format: date-time */
            endsAt: string | null;
            /** Format: int32 */
            goalAmount: number;
            /** Format: int64 */
            id: number | null;
            /** Format: int32 */
            menaSupport: number;
            organization: string | null;
            /** Format: int32 */
            raisedAmount: number;
            title: string | null;
            type: string | null;
            zone: string | null;
        };
        PublicPartner: {
            /** Format: int64 */
            id: number | null;
            name: string | null;
            organization: string | null;
            zone: string | null;
        };
        Reason: {
            reason: string | null;
        };
        Reassign: {
            /** Format: int64 */
            providerId: number | null;
        };
        ReceivedGift: {
            /** Format: date-time */
            createdAt: string | null;
            /** Format: int64 */
            id: number | null;
            status: string | null;
            title: string | null;
        };
        RequestRow: {
            /** Format: int32 */
            amountPerOccurrence: number;
            /** Format: int32 */
            annexCost: number;
            annexServices: Record<string, never> | null;
            /** Format: int64 */
            beneficiaryId: number | null;
            category: string | null;
            /** Format: date-time */
            closedAt: string | null;
            /** Format: date-time */
            completedAt: string | null;
            /** Format: date-time */
            createdAt: string | null;
            description: string | null;
            /** Format: int32 */
            executionBudget: number;
            /** Format: int64 */
            id: number | null;
            infoRequest: string | null;
            kind: string | null;
            /** Format: int32 */
            occurrences: number;
            /** Format: int32 */
            otherFees: number;
            /** Format: int64 */
            partnerId: number | null;
            paymentMode: string | null;
            recurrence: string | null;
            ref: string | null;
            rejectionReason: string | null;
            /** Format: date-time */
            startDate: string | null;
            status: string | null;
            targetName: string | null;
            targetZone: string | null;
            title: string | null;
            /** Format: int32 */
            totalAmount: number;
            /** Format: date-time */
            updatedAt: string | null;
            /** Format: int64 */
            userId: number | null;
        };
        Respond: {
            decision: string | null;
            reason: string | null;
        };
        Review: {
            comment: string | null;
            /** Format: date-time */
            createdAt: string | null;
            /** Format: int64 */
            fromUserId: number | null;
            /** Format: int64 */
            id: number | null;
            kind: string | null;
            /** Format: int32 */
            rating: number;
            /** Format: int64 */
            refId: number | null;
            /** Format: int64 */
            toUserId: number | null;
        };
        ReviewRequest: {
            comment: string | null;
            /** Format: int32 */
            rating: number;
        };
        Rule: {
            active: boolean;
            /** Format: int32 */
            amount: number;
            /** Format: int32 */
            dailyCap: number;
            eventKey: string | null;
            /** Format: int64 */
            id: number | null;
            label: string | null;
        };
        RuleRequest: {
            active: boolean;
            /** Format: int32 */
            amount: number;
            /** Format: int32 */
            dailyCap: number;
            eventKey: string | null;
            label: string | null;
        };
        SaveCatalogItemRequest: {
            active: boolean;
            category: string | null;
            /** Format: int32 */
            costEstimate: number;
            description: string | null;
            emoji: string | null;
            title: string | null;
            unit: string | null;
            /** Format: int32 */
            unitPrice: number;
        };
        SaveSettingRequest: {
            value: components["schemas"]["JsonNode"] | null;
        };
        SaveTarget: {
            active: boolean;
            description: string | null;
            label: string | null;
        };
        Schedule: {
            /** Format: int32 */
            occurrences: number | null;
            paymentMode: string | null;
            recurrence: string | null;
            startDate: string | null;
        };
        Service: {
            active: boolean;
            category: string | null;
            description: string | null;
            /** Format: int64 */
            id: number | null;
            label: string | null;
            /** Format: int64 */
            providerId: number | null;
            unit: string | null;
            /** Format: int32 */
            unitCost: number;
        };
        ServiceMessage: {
            /** Format: int64 */
            authorId: number | null;
            body: string | null;
            /** Format: date-time */
            createdAt: string | null;
            /** Format: int64 */
            id: number | null;
            /** Format: int64 */
            orderId: number | null;
            /** Format: int32 */
            price: number | null;
        };
        ServiceMessageView: {
            authorName: string | null;
            message: components["schemas"]["ServiceMessage"] | null;
        };
        ServicePayment: {
            /** Format: int32 */
            amountPaid: number;
            /** Format: int32 */
            amountPlanned: number;
            exchangeStatus: string | null;
            /** Format: int64 */
            id: number | null;
            mode: string | null;
            status: string | null;
        };
        ServiceRequest: {
            category: string | null;
            description: string | null;
            label: string | null;
            unit: string | null;
            /** Format: int32 */
            unitCost: number;
        };
        SessionResponse: {
            accessToken: string | null;
            /** Format: date-time */
            expiresAt: string | null;
            tokenType: string | null;
            user: components["schemas"]["IdentityUserResponse"] | null;
        };
        SetCatalogItemActiveRequest: {
            active: boolean;
        };
        SetRoleRequest: {
            permissions: string[];
            role: string | null;
        };
        SetStatusRequest: {
            status: string | null;
        };
        SettingView: {
            description: string | null;
            key: string | null;
            /** Format: date-time */
            updatedAt: string | null;
            /** Format: int64 */
            updatedBy: number | null;
            value: components["schemas"]["JsonNode"] | null;
        };
        SimulateRequest: {
            outcome: string | null;
        };
        SmsOverview: {
            recent: components["schemas"]["SmsRow"][];
            summary: components["schemas"]["SmsSummary"] | null;
        };
        SmsRow: {
            /** Format: int32 */
            attempts: number;
            /** Format: date-time */
            createdAt: string | null;
            /** Format: int64 */
            id: number | null;
            lastError: string | null;
            /** Format: date-time */
            nextAttemptAt: string | null;
            notificationType: string | null;
            phone: string | null;
            /** Format: date-time */
            sentAt: string | null;
            status: string | null;
        };
        SmsSummary: {
            /** Format: int64 */
            failed: number;
            /** Format: int64 */
            pending: number;
            /** Format: int64 */
            sent: number;
        };
        StatRow: {
            late: boolean;
            providers: string | null;
            request: components["schemas"]["RequestRow"] | null;
        };
        Stats: {
            kpi: components["schemas"]["Kpi"] | null;
            rows: components["schemas"]["StatRow"][];
        };
        Status: {
            label: string | null;
            tone: string | null;
        };
        Support: {
            /** Format: int32 */
            amount: number;
        };
        Target: {
            active: boolean;
            description: string | null;
            id: string | null;
            label: string | null;
        };
        TargetUsage: {
            /** Format: int64 */
            count: number;
            target: string | null;
        };
        TransactionView: {
            /** Format: int32 */
            amount: number;
            /** Format: date-time */
            createdAt: string | null;
            /** Format: int64 */
            createdBy: number | null;
            eventKey: string | null;
            /** Format: int64 */
            id: number | null;
            reason: string | null;
            status: string | null;
            type: string | null;
            /** Format: int64 */
            userId: number | null;
        };
        TransferRequest: {
            /** Format: int32 */
            amount: number;
            phone: string | null;
        };
        Transition: {
            action: string | null;
            comment: string | null;
            /** Format: int32 */
            rating: number | null;
        };
        UserOption: {
            /** Format: int64 */
            id: number | null;
            name: string | null;
            phone: string | null;
            role: string | null;
        };
        VerifyRequest: {
            verified: boolean;
        };
    };
    responses: never;
    parameters: never;
    requestBodies: never;
    headers: never;
    pathItems: never;
}
export type $defs = Record<string, never>;
export interface operations {
    managedCatalog: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ManagedCatalogItemResponse"][];
                };
            };
        };
    };
    create_3: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SaveCatalogItemRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ManagedCatalogItemResponse"];
                };
            };
        };
    };
    update: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SaveCatalogItemRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ManagedCatalogItemResponse"];
                };
            };
        };
    };
    setActive_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SetCatalogItemActiveRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ManagedCatalogItemResponse"];
                };
            };
        };
    };
    logout_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
    me_2: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["IdentityUserResponse"];
                };
            };
        };
    };
    updateMe_2: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ProfileUpdate"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["IdentityUserResponse"];
                };
            };
        };
    };
    requestOtp: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["OtpRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["OtpResponse"];
                };
            };
        };
    };
    verifyOtp: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["OtpVerification"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SessionResponse"];
                };
            };
        };
    };
    permissions_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": string[];
                };
            };
        };
    };
    refresh_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SessionResponse"];
                };
            };
        };
    };
    publicCatalog_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["CatalogResponse"];
                };
            };
        };
    };
    health: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    audit: {
        parameters: {
            query?: {
                action?: string;
                actor?: number;
                actorName?: string;
                from?: string;
                q?: string;
                to?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AuditView"][];
                };
            };
        };
    };
    beneficiaries: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["BeneficiaryRow"][];
                };
            };
        };
    };
    createBeneficiary: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["BeneficiaryRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    archive: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["Archive"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    managedCatalog_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ManagedCatalogItemResponse"][];
                };
            };
        };
    };
    create_4: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SaveCatalogItemRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ManagedCatalogItemResponse"];
                };
            };
        };
    };
    update_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SaveCatalogItemRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ManagedCatalogItemResponse"];
                };
            };
        };
    };
    setActive: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SetCatalogItemActiveRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ManagedCatalogItemResponse"];
                };
            };
        };
    };
    all: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["Target"][];
                };
            };
        };
    };
    create_2: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SaveTarget"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["Target"];
                };
            };
        };
    };
    update_2: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SaveTarget"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["Target"];
                };
            };
        };
    };
    delete: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    contactMessages: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ContactView"][];
                };
            };
        };
    };
    decideContribution: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ContributionDecision"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    dashboard_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminDashboard"];
                };
            };
        };
    };
    disputes: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminDispute"][];
                };
            };
        };
    };
    updateDispute: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["DisputeUpdate"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    finances: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["Finances"];
                };
            };
        };
    };
    installmentDone: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    adminList_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminListings"];
                };
            };
        };
    };
    moderate: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["Moderate"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    missions: {
        parameters: {
            query?: {
                status?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminMissions"];
                };
            };
        };
    };
    cancelMission: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["Reason"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    reassign: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["Reassign"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    validate: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    recent: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PaymentTransaction"][];
                };
            };
        };
    };
    payments: {
        parameters: {
            query?: {
                direction?: string;
                status?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminPayments"];
                };
            };
        };
    };
    updatePayment: {
        parameters: {
            query: {
                amountPaid?: number;
                amountValidated?: number;
                exchangeStatus?: string;
                note?: string;
                reference?: string;
                status: string;
            };
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: {
            content: {
                "multipart/form-data": {
                    /** Format: binary */
                    receipt?: string;
                };
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    payout: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["PayRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PaymentTransactionView"];
                };
            };
        };
    };
    proofs: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminProof"][];
                };
            };
        };
    };
    reviewProof: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ProofReview"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    adminList: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProviderCard"][];
                };
            };
        };
    };
    verify: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["VerifyRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    directory: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProviderCard"][];
                };
            };
        };
    };
    options_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProviderOption"][];
                };
            };
        };
    };
    requests: {
        parameters: {
            query?: {
                q?: string;
                status?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminRequestRow"][];
                };
            };
        };
    };
    request_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminRequestDetail"];
                };
            };
        };
    };
    linkBeneficiary: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["LinkBeneficiary"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    close: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    complete_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    decide_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["Decision"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    finance: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["Finance"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    createMission: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateMission"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    targetUsage: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TargetUsage"][];
                };
            };
        };
    };
    overview: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminOverview"];
                };
            };
        };
    };
    accounts: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AccountView"][];
                };
            };
        };
    };
    adjust: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["AdjustRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    createRule: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["RuleRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    saveRule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["RuleRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    deleteRule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    decide: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["DecisionRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    allSettings: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SettingView"][];
                };
            };
        };
    };
    saveSetting: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                key: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SaveSettingRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    sms: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SmsOverview"];
                };
            };
        };
    };
    stats: {
        parameters: {
            query?: {
                category?: string;
                from?: string;
                provider?: number;
                status?: string;
                to?: string;
                zone?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["Stats"];
                };
            };
        };
    };
    export: {
        parameters: {
            query?: {
                category?: string;
                from?: string;
                provider?: number;
                status?: string;
                to?: string;
                zone?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": string;
                };
            };
        };
    };
    list: {
        parameters: {
            query?: {
                q?: string;
                role?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminUserView"][];
                };
            };
        };
    };
    setRole: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SetRoleRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminUserView"];
                };
            };
        };
    };
    setStatus: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SetStatusRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminUserView"];
                };
            };
        };
    };
    options: {
        parameters: {
            query?: {
                role?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["UserOption"][];
                };
            };
        };
    };
    logout: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
    me_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["IdentityUserResponse"];
                };
            };
        };
    };
    updateMe_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ProfileUpdate"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["IdentityUserResponse"];
                };
            };
        };
    };
    requestOtp_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["OtpRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["OtpResponse"];
                };
            };
        };
    };
    verifyOtp_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["OtpVerification"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SessionResponse"];
                };
            };
        };
    };
    permissions: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": string[];
                };
            };
        };
    };
    refresh: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SessionResponse"];
                };
            };
        };
    };
    received: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ReceivedGift"][];
                };
            };
        };
    };
    cart: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["Cart"];
                };
            };
        };
    };
    addToCart: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["AddToCart"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    removeCartItem: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    checkout: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CheckoutRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["Created"];
                };
            };
        };
    };
    publicCatalog: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["CatalogResponse"];
                };
            };
        };
    };
    active: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["Target"][];
                };
            };
        };
    };
    contact: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ContactRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    dispute: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DisputeDetail"];
                };
            };
        };
    };
    disputeMessage: {
        parameters: {
            query: {
                body: string;
            };
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: {
            content: {
                "multipart/form-data": {
                    /** Format: binary */
                    attachment?: string;
                };
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    file: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
                kind: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": string;
                };
            };
        };
    };
    create_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateListing"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    contribute: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["Contribute"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    supportMena: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["Support"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    dashboard: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PartnerDashboard"];
                };
            };
        };
    };
    mine_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ListingView"][];
                };
            };
        };
    };
    published: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ListingView"][];
                };
            };
        };
    };
    mission: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProviderMissionDetail"];
                };
            };
        };
    };
    complete: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    respond: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["Respond"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    start: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    providerDashboard: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProviderDashboard"];
                };
            };
        };
    };
    myMissions: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["MissionRow"][];
                };
            };
        };
    };
    notifications: {
        parameters: {
            query?: {
                limit?: number;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["NotificationView"][];
                };
            };
        };
    };
    readAll: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: boolean;
                    };
                };
            };
        };
    };
    unread: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: number;
                    };
                };
            };
        };
    };
    declare: {
        parameters: {
            query: {
                method: string;
                reference?: string;
            };
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: {
            content: {
                "multipart/form-data": {
                    /** Format: binary */
                    receipt?: string;
                };
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    pay: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["PayRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PaymentTransactionView"];
                };
            };
        };
    };
    transactions: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PaymentTransaction"][];
                };
            };
        };
    };
    myPayments: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PaymentRow"][];
                };
            };
        };
    };
    config: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["OnlinePaymentConfig"];
                };
            };
        };
    };
    simulate: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                txId: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SimulateRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PaymentTransactionView"];
                };
            };
        };
    };
    webhook: {
        parameters: {
            query?: never;
            header?: {
                "X-Menewa-Signature"?: string;
            };
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": string;
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
    uploadProof: {
        parameters: {
            query: {
                caption?: string;
                hidden?: boolean;
                missionId?: number;
                requestId?: number;
                type: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: {
            content: {
                "multipart/form-data": {
                    /** Format: binary */
                    file: string;
                };
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    me: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["MyProvider"];
                };
            };
        };
    };
    updateMe: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ProfileRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    addService: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ServiceRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    removeService: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    search: {
        parameters: {
            query?: {
                cat?: string;
                q?: string;
                zone?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProviderCard"][];
                };
            };
        };
    };
    publicListings: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PublicListing"][];
                };
            };
        };
    };
    partners: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PublicPartner"][];
                };
            };
        };
    };
    publicProviders: {
        parameters: {
            query?: {
                cat?: string;
                q?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProviderCard"][];
                };
            };
        };
    };
    reference: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["Catalog"];
                };
            };
        };
    };
    myRequests: {
        parameters: {
            query?: {
                status?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["RequestRow"][];
                };
            };
        };
    };
    createCustom: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CustomRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["Created"];
                };
            };
        };
    };
    request: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DonorRequestDetail"];
                };
            };
        };
    };
    answer: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["Answer"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    cancel: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    openDispute: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["DisputeRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    review: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ReviewRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    donorDashboard: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DonorDashboard"];
                };
            };
        };
    };
    conversion: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ConversionRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    me_3: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["MyRewards"];
                };
            };
        };
    };
    transfer: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["TransferRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    mine: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["OrderView"][];
                };
            };
        };
    };
    create: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateOrder"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: Record<string, never>;
                    };
                };
            };
        };
    };
    detail: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["OrderDetail"];
                };
            };
        };
    };
    message: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["PostMessage"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    transition: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["Transition"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: string;
                    };
                };
            };
        };
    };
    publicSettings: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": {
                        [key: string]: components["schemas"]["JsonNode"];
                    };
                };
            };
        };
    };
}
