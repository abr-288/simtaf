// Généré par scripts/contract.mjs depuis docs/api/openapi.json — ne pas modifier à la main.
// ignore_for_file: type=lint, unused_element

int _int(Object? v) => v is num ? v.toInt() : int.tryParse('$v') ?? 0;
double _dbl(Object? v) => v is num ? v.toDouble() : double.tryParse('$v') ?? 0;
String _str(Object? v) => v?.toString() ?? '';
Map<String, dynamic> _map(Object? v) => v is Map ? Map<String, dynamic>.from(v) : <String, dynamic>{};
List<T> _list<T>(Object? v, T Function(Object? e) f) => v is List ? v.map(f).toList() : <T>[];

class ApiAccountView {
  const ApiAccountView({required this.balance, required this.earned, this.userId});

  factory ApiAccountView.fromJson(Map<String, dynamic> j) => ApiAccountView(
        balance: _int(j['balance']),
        earned: _int(j['earned']),
        userId: j['userId'] == null ? null : _int(j['userId']),
      );

  final int balance;
  final int earned;
  final int? userId;
}

class ApiAddToCart {
  const ApiAddToCart({required this.catalogItemId, required this.quantity, this.target});

  factory ApiAddToCart.fromJson(Map<String, dynamic> j) => ApiAddToCart(
        catalogItemId: _int(j['catalogItemId']),
        quantity: _int(j['quantity']),
        target: j['target']?.toString(),
      );

  final int catalogItemId;
  final int quantity;
  final String? target;
}

class ApiAdjustRequest {
  const ApiAdjustRequest({required this.amount, this.reason, this.userId});

  factory ApiAdjustRequest.fromJson(Map<String, dynamic> j) => ApiAdjustRequest(
        amount: _int(j['amount']),
        reason: j['reason']?.toString(),
        userId: j['userId'] == null ? null : _int(j['userId']),
      );

  final int amount;
  final String? reason;
  final int? userId;
}

class ApiAdminDashboard {
  const ApiAdminDashboard({required this.collected, required this.completed, required this.infoRequested, required this.lateMissions, required this.openDisputes, required this.paymentsToFollow, required this.proofsToReview, required this.recent, required this.toTreat, required this.users});

  factory ApiAdminDashboard.fromJson(Map<String, dynamic> j) => ApiAdminDashboard(
        collected: _int(j['collected']),
        completed: _int(j['completed']),
        infoRequested: _int(j['infoRequested']),
        lateMissions: _int(j['lateMissions']),
        openDisputes: _int(j['openDisputes']),
        paymentsToFollow: _int(j['paymentsToFollow']),
        proofsToReview: _int(j['proofsToReview']),
        recent: _list(j['recent'], (e) => ApiRequestRow.fromJson(_map(e))),
        toTreat: _int(j['toTreat']),
        users: _int(j['users']),
      );

  final int collected;
  final int completed;
  final int infoRequested;
  final int lateMissions;
  final int openDisputes;
  final int paymentsToFollow;
  final int proofsToReview;
  final List<ApiRequestRow> recent;
  final int toTreat;
  final int users;
}

class ApiAdminDispute {
  const ApiAdminDispute({this.dispute, this.openedByName});

  factory ApiAdminDispute.fromJson(Map<String, dynamic> j) => ApiAdminDispute(
        dispute: j['dispute'] is Map ? ApiDisputeRow.fromJson(_map(j['dispute'])) : null,
        openedByName: j['openedByName']?.toString(),
      );

  final ApiDisputeRow? dispute;
  final String? openedByName;
}

class ApiAdminListings {
  const ApiAdminListings({required this.listings, required this.pendingContributions});

  factory ApiAdminListings.fromJson(Map<String, dynamic> j) => ApiAdminListings(
        listings: _list(j['listings'], (e) => ApiListingView.fromJson(_map(e))),
        pendingContributions: _list(j['pendingContributions'], (e) => ApiContributionView.fromJson(_map(e))),
      );

  final List<ApiListingView> listings;
  final List<ApiContributionView> pendingContributions;
}

class ApiAdminMission {
  const ApiAdminMission({this.mission, this.providerName, this.requestRef});

  factory ApiAdminMission.fromJson(Map<String, dynamic> j) => ApiAdminMission(
        mission: j['mission'] is Map ? ApiMissionRow.fromJson(_map(j['mission'])) : null,
        providerName: j['providerName']?.toString(),
        requestRef: j['requestRef']?.toString(),
      );

  final ApiMissionRow? mission;
  final String? providerName;
  final String? requestRef;
}

class ApiAdminMissions {
  const ApiAdminMissions({required this.missions, required this.providers});

  factory ApiAdminMissions.fromJson(Map<String, dynamic> j) => ApiAdminMissions(
        missions: _list(j['missions'], (e) => ApiAdminMission.fromJson(_map(e))),
        providers: _list(j['providers'], (e) => ApiProviderOption.fromJson(_map(e))),
      );

  final List<ApiAdminMission> missions;
  final List<ApiProviderOption> providers;
}

class ApiAdminOverview {
  const ApiAdminOverview({required this.circulating, required this.events, required this.manualAdjustMax, required this.rules, required this.transactions});

  factory ApiAdminOverview.fromJson(Map<String, dynamic> j) => ApiAdminOverview(
        circulating: _int(j['circulating']),
        events: _list(j['events'], (e) => ApiEventOption.fromJson(_map(e))),
        manualAdjustMax: _int(j['manualAdjustMax']),
        rules: _list(j['rules'], (e) => ApiRule.fromJson(_map(e))),
        transactions: _list(j['transactions'], (e) => ApiAdminTransaction.fromJson(_map(e))),
      );

  final int circulating;
  final List<ApiEventOption> events;
  final int manualAdjustMax;
  final List<ApiRule> rules;
  final List<ApiAdminTransaction> transactions;
}

class ApiAdminPayment {
  const ApiAdminPayment({this.payment, this.userName, this.userPhone});

  factory ApiAdminPayment.fromJson(Map<String, dynamic> j) => ApiAdminPayment(
        payment: j['payment'] is Map ? ApiPaymentRow.fromJson(_map(j['payment'])) : null,
        userName: j['userName']?.toString(),
        userPhone: j['userPhone']?.toString(),
      );

  final ApiPaymentRow? payment;
  final String? userName;
  final String? userPhone;
}

class ApiAdminPayments {
  const ApiAdminPayments({required this.paid, required this.payments, required this.planned});

  factory ApiAdminPayments.fromJson(Map<String, dynamic> j) => ApiAdminPayments(
        paid: _int(j['paid']),
        payments: _list(j['payments'], (e) => ApiAdminPayment.fromJson(_map(e))),
        planned: _int(j['planned']),
      );

  final int paid;
  final List<ApiAdminPayment> payments;
  final int planned;
}

class ApiAdminProof {
  const ApiAdminProof({this.proof, this.requestRef, this.uploaderName});

  factory ApiAdminProof.fromJson(Map<String, dynamic> j) => ApiAdminProof(
        proof: j['proof'] is Map ? ApiProofRow.fromJson(_map(j['proof'])) : null,
        requestRef: j['requestRef']?.toString(),
        uploaderName: j['uploaderName']?.toString(),
      );

  final ApiProofRow? proof;
  final String? requestRef;
  final String? uploaderName;
}

class ApiAdminRequestDetail {
  const ApiAdminRequestDetail({required this.beneficiaries, this.beneficiary, required this.checks, required this.disputes, this.donorName, this.donorPhone, required this.history, required this.installments, required this.items, this.margin, required this.missions, required this.payments, required this.proofs, required this.providers, this.request, required this.targetMarginPct});

  factory ApiAdminRequestDetail.fromJson(Map<String, dynamic> j) => ApiAdminRequestDetail(
        beneficiaries: _list(j['beneficiaries'], (e) => ApiBeneficiaryRow.fromJson(_map(e))),
        beneficiary: j['beneficiary'] is Map ? ApiBeneficiaryRow.fromJson(_map(j['beneficiary'])) : null,
        checks: _list(j['checks'], (e) => ApiCheck.fromJson(_map(e))),
        disputes: _list(j['disputes'], (e) => ApiDisputeRow.fromJson(_map(e))),
        donorName: j['donorName']?.toString(),
        donorPhone: j['donorPhone']?.toString(),
        history: _list(j['history'], (e) => ApiHistoryRow.fromJson(_map(e))),
        installments: _list(j['installments'], (e) => ApiInstallmentRow.fromJson(_map(e))),
        items: _list(j['items'], (e) => ApiItemRow.fromJson(_map(e))),
        margin: j['margin'] is Map ? ApiMargin.fromJson(_map(j['margin'])) : null,
        missions: _list(j['missions'], (e) => ApiAdminMission.fromJson(_map(e))),
        payments: _list(j['payments'], (e) => ApiPaymentRow.fromJson(_map(e))),
        proofs: _list(j['proofs'], (e) => ApiProofRow.fromJson(_map(e))),
        providers: _list(j['providers'], (e) => ApiProviderOption.fromJson(_map(e))),
        request: j['request'] is Map ? ApiRequestRow.fromJson(_map(j['request'])) : null,
        targetMarginPct: _int(j['targetMarginPct']),
      );

  final List<ApiBeneficiaryRow> beneficiaries;
  final ApiBeneficiaryRow? beneficiary;
  final List<ApiCheck> checks;
  final List<ApiDisputeRow> disputes;
  final String? donorName;
  final String? donorPhone;
  final List<ApiHistoryRow> history;
  final List<ApiInstallmentRow> installments;
  final List<ApiItemRow> items;
  final ApiMargin? margin;
  final List<ApiAdminMission> missions;
  final List<ApiPaymentRow> payments;
  final List<ApiProofRow> proofs;
  final List<ApiProviderOption> providers;
  final ApiRequestRow? request;
  final int targetMarginPct;
}

class ApiAdminRequestRow {
  const ApiAdminRequestRow({this.donorName, this.donorPhone, this.request});

  factory ApiAdminRequestRow.fromJson(Map<String, dynamic> j) => ApiAdminRequestRow(
        donorName: j['donorName']?.toString(),
        donorPhone: j['donorPhone']?.toString(),
        request: j['request'] is Map ? ApiRequestRow.fromJson(_map(j['request'])) : null,
      );

  final String? donorName;
  final String? donorPhone;
  final ApiRequestRow? request;
}

class ApiAdminTransaction {
  const ApiAdminTransaction({required this.amount, this.createdAt, this.createdBy, this.id, this.reason, this.status, this.type, this.userId, this.userName});

  factory ApiAdminTransaction.fromJson(Map<String, dynamic> j) => ApiAdminTransaction(
        amount: _int(j['amount']),
        createdAt: j['createdAt']?.toString(),
        createdBy: j['createdBy'] == null ? null : _int(j['createdBy']),
        id: j['id'] == null ? null : _int(j['id']),
        reason: j['reason']?.toString(),
        status: j['status']?.toString(),
        type: j['type']?.toString(),
        userId: j['userId'] == null ? null : _int(j['userId']),
        userName: j['userName']?.toString(),
      );

  final int amount;
  final String? createdAt;
  final int? createdBy;
  final int? id;
  final String? reason;
  final String? status;
  final String? type;
  final int? userId;
  final String? userName;
}

class ApiAdminUserView {
  const ApiAdminUserView({this.createdAt, this.id, this.lastLoginAt, this.name, this.organization, required this.permissions, this.phone, this.role, this.status, this.zone});

  factory ApiAdminUserView.fromJson(Map<String, dynamic> j) => ApiAdminUserView(
        createdAt: j['createdAt']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        lastLoginAt: j['lastLoginAt']?.toString(),
        name: j['name']?.toString(),
        organization: j['organization']?.toString(),
        permissions: _list(j['permissions'], (e) => _str(e)),
        phone: j['phone']?.toString(),
        role: j['role']?.toString(),
        status: j['status']?.toString(),
        zone: j['zone']?.toString(),
      );

  final String? createdAt;
  final int? id;
  final String? lastLoginAt;
  final String? name;
  final String? organization;
  final List<String> permissions;
  final String? phone;
  final String? role;
  final String? status;
  final String? zone;
}

class ApiAnnex {
  const ApiAnnex({required this.officiant, this.other, required this.packaging, this.packagingNotes, required this.photos, required this.video});

  factory ApiAnnex.fromJson(Map<String, dynamic> j) => ApiAnnex(
        officiant: j['officiant'] == true,
        other: j['other']?.toString(),
        packaging: j['packaging'] == true,
        packagingNotes: j['packagingNotes']?.toString(),
        photos: j['photos'] == true,
        video: j['video'] == true,
      );

  final bool officiant;
  final String? other;
  final bool packaging;
  final String? packagingNotes;
  final bool photos;
  final bool video;
}

class ApiAnswer {
  const ApiAnswer({this.answer});

  factory ApiAnswer.fromJson(Map<String, dynamic> j) => ApiAnswer(
        answer: j['answer']?.toString(),
      );

  final String? answer;
}

class ApiArchive {
  const ApiArchive({required this.restore});

  factory ApiArchive.fromJson(Map<String, dynamic> j) => ApiArchive(
        restore: j['restore'] == true,
      );

  final bool restore;
}

class ApiAuditView {
  const ApiAuditView({this.action, this.actorId, this.actorName, this.actorRole, this.createdAt, this.details, this.entity, this.entityId, this.id, this.ip});

  factory ApiAuditView.fromJson(Map<String, dynamic> j) => ApiAuditView(
        action: j['action']?.toString(),
        actorId: j['actorId'] == null ? null : _int(j['actorId']),
        actorName: j['actorName']?.toString(),
        actorRole: j['actorRole']?.toString(),
        createdAt: j['createdAt']?.toString(),
        details: j['details']?.toString(),
        entity: j['entity']?.toString(),
        entityId: j['entityId']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        ip: j['ip']?.toString(),
      );

  final String? action;
  final int? actorId;
  final String? actorName;
  final String? actorRole;
  final String? createdAt;
  final String? details;
  final String? entity;
  final String? entityId;
  final int? id;
  final String? ip;
}

class ApiBadgeView {
  const ApiBadgeView({this.code, this.desc, required this.earned, this.emoji, this.label});

  factory ApiBadgeView.fromJson(Map<String, dynamic> j) => ApiBadgeView(
        code: j['code']?.toString(),
        desc: j['desc']?.toString(),
        earned: j['earned'] == true,
        emoji: j['emoji']?.toString(),
        label: j['label']?.toString(),
      );

  final String? code;
  final String? desc;
  final bool earned;
  final String? emoji;
  final String? label;
}

class ApiBeneficiaryContact {
  const ApiBeneficiaryContact({this.address, this.fullName, this.phone, this.zone});

  factory ApiBeneficiaryContact.fromJson(Map<String, dynamic> j) => ApiBeneficiaryContact(
        address: j['address']?.toString(),
        fullName: j['fullName']?.toString(),
        phone: j['phone']?.toString(),
        zone: j['zone']?.toString(),
      );

  final String? address;
  final String? fullName;
  final String? phone;
  final String? zone;
}

class ApiBeneficiaryRequest {
  const ApiBeneficiaryRequest({this.address, required this.consent, this.fullName, this.needs, this.phone, this.userId, this.zone});

  factory ApiBeneficiaryRequest.fromJson(Map<String, dynamic> j) => ApiBeneficiaryRequest(
        address: j['address']?.toString(),
        consent: j['consent'] == true,
        fullName: j['fullName']?.toString(),
        needs: j['needs']?.toString(),
        phone: j['phone']?.toString(),
        userId: j['userId'] == null ? null : _int(j['userId']),
        zone: j['zone']?.toString(),
      );

  final String? address;
  final bool consent;
  final String? fullName;
  final String? needs;
  final String? phone;
  final int? userId;
  final String? zone;
}

class ApiBeneficiaryRow {
  const ApiBeneficiaryRow({this.address, this.archivedAt, required this.consent, this.createdAt, this.createdBy, this.fullName, this.id, this.needs, this.notes, this.phone, this.userId, this.zone});

  factory ApiBeneficiaryRow.fromJson(Map<String, dynamic> j) => ApiBeneficiaryRow(
        address: j['address']?.toString(),
        archivedAt: j['archivedAt']?.toString(),
        consent: j['consent'] == true,
        createdAt: j['createdAt']?.toString(),
        createdBy: j['createdBy'] == null ? null : _int(j['createdBy']),
        fullName: j['fullName']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        needs: j['needs']?.toString(),
        notes: j['notes']?.toString(),
        phone: j['phone']?.toString(),
        userId: j['userId'] == null ? null : _int(j['userId']),
        zone: j['zone']?.toString(),
      );

  final String? address;
  final String? archivedAt;
  final bool consent;
  final String? createdAt;
  final int? createdBy;
  final String? fullName;
  final int? id;
  final String? needs;
  final String? notes;
  final String? phone;
  final int? userId;
  final String? zone;
}

class ApiCart {
  const ApiCart({required this.annexPrices, required this.lines, required this.total});

  factory ApiCart.fromJson(Map<String, dynamic> j) => ApiCart(
        annexPrices: _map(j['annexPrices']).map((k, e) => MapEntry(k, _int(e))),
        lines: _list(j['lines'], (e) => ApiCartLine.fromJson(_map(e))),
        total: _int(j['total']),
      );

  final Map<String, int> annexPrices;
  final List<ApiCartLine> lines;
  final int total;
}

class ApiCartLine {
  const ApiCartLine({this.catalogItemId, this.category, this.emoji, this.id, required this.quantity, this.target, this.title, required this.unitPrice});

  factory ApiCartLine.fromJson(Map<String, dynamic> j) => ApiCartLine(
        catalogItemId: j['catalogItemId'] == null ? null : _int(j['catalogItemId']),
        category: j['category']?.toString(),
        emoji: j['emoji']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        quantity: _int(j['quantity']),
        target: j['target']?.toString(),
        title: j['title']?.toString(),
        unitPrice: _int(j['unitPrice']),
      );

  final int? catalogItemId;
  final String? category;
  final String? emoji;
  final int? id;
  final int quantity;
  final String? target;
  final String? title;
  final int unitPrice;
}

class ApiCatalog {
  const ApiCatalog({required this.labels, required this.selfRegisterRoles, required this.statuses});

  factory ApiCatalog.fromJson(Map<String, dynamic> j) => ApiCatalog(
        labels: _map(j['labels']).map((k, e) => MapEntry(k, _map(e).map((k, e) => MapEntry(k, _str(e))))),
        selfRegisterRoles: _list(j['selfRegisterRoles'], (e) => _str(e)),
        statuses: _map(j['statuses']).map((k, e) => MapEntry(k, _map(e).map((k, e) => MapEntry(k, ApiStatus.fromJson(_map(e)))))),
      );

  final Map<String, Map<String, String>> labels;
  final List<String> selfRegisterRoles;
  final Map<String, Map<String, ApiStatus>> statuses;
}

class ApiCatalogItemResponse {
  const ApiCatalogItemResponse({this.category, this.description, this.emoji, this.id, this.kind, this.slug, this.title, this.unit, required this.unitPrice});

  factory ApiCatalogItemResponse.fromJson(Map<String, dynamic> j) => ApiCatalogItemResponse(
        category: j['category']?.toString(),
        description: j['description']?.toString(),
        emoji: j['emoji']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        kind: j['kind']?.toString(),
        slug: j['slug']?.toString(),
        title: j['title']?.toString(),
        unit: j['unit']?.toString(),
        unitPrice: _int(j['unitPrice']),
      );

  final String? category;
  final String? description;
  final String? emoji;
  final int? id;
  final String? kind;
  final String? slug;
  final String? title;
  final String? unit;
  final int unitPrice;
}

class ApiCatalogResponse {
  const ApiCatalogResponse({this.currency, required this.items});

  factory ApiCatalogResponse.fromJson(Map<String, dynamic> j) => ApiCatalogResponse(
        currency: j['currency']?.toString(),
        items: _list(j['items'], (e) => ApiCatalogItemResponse.fromJson(_map(e))),
      );

  final String? currency;
  final List<ApiCatalogItemResponse> items;
}

class ApiCheck {
  const ApiCheck({this.help, this.label, required this.ok});

  factory ApiCheck.fromJson(Map<String, dynamic> j) => ApiCheck(
        help: j['help']?.toString(),
        label: j['label']?.toString(),
        ok: j['ok'] == true,
      );

  final String? help;
  final String? label;
  final bool ok;
}

class ApiCheckoutRequest {
  const ApiCheckoutRequest({this.annex, this.description, this.idempotencyKey, this.schedule, this.targetName, this.targetZone});

  factory ApiCheckoutRequest.fromJson(Map<String, dynamic> j) => ApiCheckoutRequest(
        annex: j['annex'] is Map ? ApiAnnex.fromJson(_map(j['annex'])) : null,
        description: j['description']?.toString(),
        idempotencyKey: j['idempotencyKey']?.toString(),
        schedule: j['schedule'] is Map ? ApiSchedule.fromJson(_map(j['schedule'])) : null,
        targetName: j['targetName']?.toString(),
        targetZone: j['targetZone']?.toString(),
      );

  final ApiAnnex? annex;
  final String? description;
  final String? idempotencyKey;
  final ApiSchedule? schedule;
  final String? targetName;
  final String? targetZone;
}

class ApiContactRequest {
  const ApiContactRequest({this.email, this.message, this.name, this.phone, this.subject});

  factory ApiContactRequest.fromJson(Map<String, dynamic> j) => ApiContactRequest(
        email: j['email']?.toString(),
        message: j['message']?.toString(),
        name: j['name']?.toString(),
        phone: j['phone']?.toString(),
        subject: j['subject']?.toString(),
      );

  final String? email;
  final String? message;
  final String? name;
  final String? phone;
  final String? subject;
}

class ApiContactView {
  const ApiContactView({this.createdAt, this.email, required this.handled, this.id, this.message, this.name, this.phone, this.subject});

  factory ApiContactView.fromJson(Map<String, dynamic> j) => ApiContactView(
        createdAt: j['createdAt']?.toString(),
        email: j['email']?.toString(),
        handled: j['handled'] == true,
        id: j['id'] == null ? null : _int(j['id']),
        message: j['message']?.toString(),
        name: j['name']?.toString(),
        phone: j['phone']?.toString(),
        subject: j['subject']?.toString(),
      );

  final String? createdAt;
  final String? email;
  final bool handled;
  final int? id;
  final String? message;
  final String? name;
  final String? phone;
  final String? subject;
}

class ApiContribute {
  const ApiContribute({required this.amount, this.method, this.reference});

  factory ApiContribute.fromJson(Map<String, dynamic> j) => ApiContribute(
        amount: _int(j['amount']),
        method: j['method']?.toString(),
        reference: j['reference']?.toString(),
      );

  final int amount;
  final String? method;
  final String? reference;
}

class ApiContribution {
  const ApiContribution({required this.amount, this.createdAt, this.id, this.listingId, this.method, this.reference, this.status, this.userId});

  factory ApiContribution.fromJson(Map<String, dynamic> j) => ApiContribution(
        amount: _int(j['amount']),
        createdAt: j['createdAt']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        listingId: j['listingId'] == null ? null : _int(j['listingId']),
        method: j['method']?.toString(),
        reference: j['reference']?.toString(),
        status: j['status']?.toString(),
        userId: j['userId'] == null ? null : _int(j['userId']),
      );

  final int amount;
  final String? createdAt;
  final int? id;
  final int? listingId;
  final String? method;
  final String? reference;
  final String? status;
  final int? userId;
}

class ApiContributionDecision {
  const ApiContributionDecision({this.decision});

  factory ApiContributionDecision.fromJson(Map<String, dynamic> j) => ApiContributionDecision(
        decision: j['decision']?.toString(),
      );

  final String? decision;
}

class ApiContributionView {
  const ApiContributionView({this.contribution, this.listingTitle, this.userName});

  factory ApiContributionView.fromJson(Map<String, dynamic> j) => ApiContributionView(
        contribution: j['contribution'] is Map ? ApiContribution.fromJson(_map(j['contribution'])) : null,
        listingTitle: j['listingTitle']?.toString(),
        userName: j['userName']?.toString(),
      );

  final ApiContribution? contribution;
  final String? listingTitle;
  final String? userName;
}

class ApiConversionRequest {
  const ApiConversionRequest({required this.amount});

  factory ApiConversionRequest.fromJson(Map<String, dynamic> j) => ApiConversionRequest(
        amount: _int(j['amount']),
      );

  final int amount;
}

class ApiCreated {
  const ApiCreated({this.id, this.ref});

  factory ApiCreated.fromJson(Map<String, dynamic> j) => ApiCreated(
        id: j['id'] == null ? null : _int(j['id']),
        ref: j['ref']?.toString(),
      );

  final int? id;
  final String? ref;
}

class ApiCreateListing {
  const ApiCreateListing({this.description, this.endsAt, required this.goalAmount, this.title, this.type, this.zone});

  factory ApiCreateListing.fromJson(Map<String, dynamic> j) => ApiCreateListing(
        description: j['description']?.toString(),
        endsAt: j['endsAt']?.toString(),
        goalAmount: _int(j['goalAmount']),
        title: j['title']?.toString(),
        type: j['type']?.toString(),
        zone: j['zone']?.toString(),
      );

  final String? description;
  final String? endsAt;
  final int goalAmount;
  final String? title;
  final String? type;
  final String? zone;
}

class ApiCreateMission {
  const ApiCreateMission({this.compensationMode, this.dueDate, this.exceptionReason, this.exchangeDescription, this.instructions, required this.providerFee, this.providerId, this.title});

  factory ApiCreateMission.fromJson(Map<String, dynamic> j) => ApiCreateMission(
        compensationMode: j['compensationMode']?.toString(),
        dueDate: j['dueDate']?.toString(),
        exceptionReason: j['exceptionReason']?.toString(),
        exchangeDescription: j['exchangeDescription']?.toString(),
        instructions: j['instructions']?.toString(),
        providerFee: _int(j['providerFee']),
        providerId: j['providerId'] == null ? null : _int(j['providerId']),
        title: j['title']?.toString(),
      );

  final String? compensationMode;
  final String? dueDate;
  final String? exceptionReason;
  final String? exchangeDescription;
  final String? instructions;
  final int providerFee;
  final int? providerId;
  final String? title;
}

class ApiCreateOrder {
  const ApiCreateOrder({this.compensationMode, this.description, this.exchangeDescription, required this.price, this.providerId, this.title, this.zone});

  factory ApiCreateOrder.fromJson(Map<String, dynamic> j) => ApiCreateOrder(
        compensationMode: j['compensationMode']?.toString(),
        description: j['description']?.toString(),
        exchangeDescription: j['exchangeDescription']?.toString(),
        price: _int(j['price']),
        providerId: j['providerId'] == null ? null : _int(j['providerId']),
        title: j['title']?.toString(),
        zone: j['zone']?.toString(),
      );

  final String? compensationMode;
  final String? description;
  final String? exchangeDescription;
  final int price;
  final int? providerId;
  final String? title;
  final String? zone;
}

class ApiCustomRequest {
  const ApiCustomRequest({required this.amount, this.annex, this.description, this.idempotencyKey, this.kind, this.schedule, this.targetName, this.targetZone, this.title});

  factory ApiCustomRequest.fromJson(Map<String, dynamic> j) => ApiCustomRequest(
        amount: _int(j['amount']),
        annex: j['annex'] is Map ? ApiAnnex.fromJson(_map(j['annex'])) : null,
        description: j['description']?.toString(),
        idempotencyKey: j['idempotencyKey']?.toString(),
        kind: j['kind']?.toString(),
        schedule: j['schedule'] is Map ? ApiSchedule.fromJson(_map(j['schedule'])) : null,
        targetName: j['targetName']?.toString(),
        targetZone: j['targetZone']?.toString(),
        title: j['title']?.toString(),
      );

  final int amount;
  final ApiAnnex? annex;
  final String? description;
  final String? idempotencyKey;
  final String? kind;
  final ApiSchedule? schedule;
  final String? targetName;
  final String? targetZone;
  final String? title;
}

class ApiDecision {
  const ApiDecision({this.decision, this.message, this.newAmount});

  factory ApiDecision.fromJson(Map<String, dynamic> j) => ApiDecision(
        decision: j['decision']?.toString(),
        message: j['message']?.toString(),
        newAmount: j['newAmount'] == null ? null : _int(j['newAmount']),
      );

  final String? decision;
  final String? message;
  final int? newAmount;
}

class ApiDecisionRequest {
  const ApiDecisionRequest({required this.approve});

  factory ApiDecisionRequest.fromJson(Map<String, dynamic> j) => ApiDecisionRequest(
        approve: j['approve'] == true,
      );

  final bool approve;
}

class ApiDisputeDetail {
  const ApiDisputeDetail({this.againstName, this.dispute, required this.messages, this.openedByName, this.responsibleName});

  factory ApiDisputeDetail.fromJson(Map<String, dynamic> j) => ApiDisputeDetail(
        againstName: j['againstName']?.toString(),
        dispute: j['dispute'] is Map ? ApiDisputeRow.fromJson(_map(j['dispute'])) : null,
        messages: _list(j['messages'], (e) => ApiDisputeMessageView.fromJson(_map(e))),
        openedByName: j['openedByName']?.toString(),
        responsibleName: j['responsibleName']?.toString(),
      );

  final String? againstName;
  final ApiDisputeRow? dispute;
  final List<ApiDisputeMessageView> messages;
  final String? openedByName;
  final String? responsibleName;
}

class ApiDisputeMessageRow {
  const ApiDisputeMessageRow({this.attachmentName, this.authorId, this.body, this.createdAt, this.disputeId, this.id});

  factory ApiDisputeMessageRow.fromJson(Map<String, dynamic> j) => ApiDisputeMessageRow(
        attachmentName: j['attachmentName']?.toString(),
        authorId: j['authorId'] == null ? null : _int(j['authorId']),
        body: j['body']?.toString(),
        createdAt: j['createdAt']?.toString(),
        disputeId: j['disputeId'] == null ? null : _int(j['disputeId']),
        id: j['id'] == null ? null : _int(j['id']),
      );

  final String? attachmentName;
  final int? authorId;
  final String? body;
  final String? createdAt;
  final int? disputeId;
  final int? id;
}

class ApiDisputeMessageView {
  const ApiDisputeMessageView({this.authorName, this.message});

  factory ApiDisputeMessageView.fromJson(Map<String, dynamic> j) => ApiDisputeMessageView(
        authorName: j['authorName']?.toString(),
        message: j['message'] is Map ? ApiDisputeMessageRow.fromJson(_map(j['message'])) : null,
      );

  final String? authorName;
  final ApiDisputeMessageRow? message;
}

class ApiDisputeRequest {
  const ApiDisputeRequest({this.description, this.subject});

  factory ApiDisputeRequest.fromJson(Map<String, dynamic> j) => ApiDisputeRequest(
        description: j['description']?.toString(),
        subject: j['subject']?.toString(),
      );

  final String? description;
  final String? subject;
}

class ApiDisputeRow {
  const ApiDisputeRow({this.againstUserId, this.closedAt, this.createdAt, this.decision, this.description, this.id, this.missionId, this.openedBy, this.ref, this.requestId, this.responsibleId, this.status, this.subject});

  factory ApiDisputeRow.fromJson(Map<String, dynamic> j) => ApiDisputeRow(
        againstUserId: j['againstUserId'] == null ? null : _int(j['againstUserId']),
        closedAt: j['closedAt']?.toString(),
        createdAt: j['createdAt']?.toString(),
        decision: j['decision']?.toString(),
        description: j['description']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        missionId: j['missionId'] == null ? null : _int(j['missionId']),
        openedBy: j['openedBy'] == null ? null : _int(j['openedBy']),
        ref: j['ref']?.toString(),
        requestId: j['requestId'] == null ? null : _int(j['requestId']),
        responsibleId: j['responsibleId'] == null ? null : _int(j['responsibleId']),
        status: j['status']?.toString(),
        subject: j['subject']?.toString(),
      );

  final int? againstUserId;
  final String? closedAt;
  final String? createdAt;
  final String? decision;
  final String? description;
  final int? id;
  final int? missionId;
  final int? openedBy;
  final String? ref;
  final int? requestId;
  final int? responsibleId;
  final String? status;
  final String? subject;
}

class ApiDisputeUpdate {
  const ApiDisputeUpdate({this.decision, this.responsibleId, this.status});

  factory ApiDisputeUpdate.fromJson(Map<String, dynamic> j) => ApiDisputeUpdate(
        decision: j['decision']?.toString(),
        responsibleId: j['responsibleId'] == null ? null : _int(j['responsibleId']),
        status: j['status']?.toString(),
      );

  final String? decision;
  final int? responsibleId;
  final String? status;
}

class ApiDonorDashboard {
  const ApiDonorDashboard({required this.done, required this.ongoing, required this.pending, required this.proofsAvailable, required this.recent});

  factory ApiDonorDashboard.fromJson(Map<String, dynamic> j) => ApiDonorDashboard(
        done: _int(j['done']),
        ongoing: _int(j['ongoing']),
        pending: _int(j['pending']),
        proofsAvailable: _int(j['proofsAvailable']),
        recent: _list(j['recent'], (e) => ApiRequestRow.fromJson(_map(e))),
      );

  final int done;
  final int ongoing;
  final int pending;
  final int proofsAvailable;
  final List<ApiRequestRow> recent;
}

class ApiDonorRequestDetail {
  const ApiDonorRequestDetail({required this.canReview, required this.disputes, required this.history, required this.installments, required this.items, required this.missions, required this.payments, required this.proofs, this.request});

  factory ApiDonorRequestDetail.fromJson(Map<String, dynamic> j) => ApiDonorRequestDetail(
        canReview: j['canReview'] == true,
        disputes: _list(j['disputes'], (e) => ApiDisputeRow.fromJson(_map(e))),
        history: _list(j['history'], (e) => ApiHistoryRow.fromJson(_map(e))),
        installments: _list(j['installments'], (e) => ApiInstallmentRow.fromJson(_map(e))),
        items: _list(j['items'], (e) => ApiItemRow.fromJson(_map(e))),
        missions: _list(j['missions'], (e) => ApiMissionSummary.fromJson(_map(e))),
        payments: _list(j['payments'], (e) => ApiPaymentRow.fromJson(_map(e))),
        proofs: _list(j['proofs'], (e) => ApiProofRow.fromJson(_map(e))),
        request: j['request'] is Map ? ApiRequestRow.fromJson(_map(j['request'])) : null,
      );

  final bool canReview;
  final List<ApiDisputeRow> disputes;
  final List<ApiHistoryRow> history;
  final List<ApiInstallmentRow> installments;
  final List<ApiItemRow> items;
  final List<ApiMissionSummary> missions;
  final List<ApiPaymentRow> payments;
  final List<ApiProofRow> proofs;
  final ApiRequestRow? request;
}

class ApiEventOption {
  const ApiEventOption({required this.base, this.key, this.label, this.who});

  factory ApiEventOption.fromJson(Map<String, dynamic> j) => ApiEventOption(
        base: j['base'] == true,
        key: j['key']?.toString(),
        label: j['label']?.toString(),
        who: j['who']?.toString(),
      );

  final bool base;
  final String? key;
  final String? label;
  final String? who;
}

class ApiFinance {
  const ApiFinance({required this.executionBudget, required this.otherFees});

  factory ApiFinance.fromJson(Map<String, dynamic> j) => ApiFinance(
        executionBudget: _int(j['executionBudget']),
        otherFees: _int(j['otherFees']),
      );

  final int executionBudget;
  final int otherFees;
}

class ApiFinanceRow {
  const ApiFinanceRow({required this.actors, required this.executionBudget, this.id, this.margin, required this.otherFees, this.partnerId, this.ref, this.title, required this.totalAmount});

  factory ApiFinanceRow.fromJson(Map<String, dynamic> j) => ApiFinanceRow(
        actors: _int(j['actors']),
        executionBudget: _int(j['executionBudget']),
        id: j['id'] == null ? null : _int(j['id']),
        margin: j['margin'] is Map ? ApiMargin.fromJson(_map(j['margin'])) : null,
        otherFees: _int(j['otherFees']),
        partnerId: j['partnerId'] == null ? null : _int(j['partnerId']),
        ref: j['ref']?.toString(),
        title: j['title']?.toString(),
        totalAmount: _int(j['totalAmount']),
      );

  final int actors;
  final int executionBudget;
  final int? id;
  final ApiMargin? margin;
  final int otherFees;
  final int? partnerId;
  final String? ref;
  final String? title;
  final int totalAmount;
}

class ApiFinances {
  const ApiFinances({required this.cost, required this.gross, required this.revenue, required this.rows, required this.targetMarginPct});

  factory ApiFinances.fromJson(Map<String, dynamic> j) => ApiFinances(
        cost: _int(j['cost']),
        gross: _int(j['gross']),
        revenue: _int(j['revenue']),
        rows: _list(j['rows'], (e) => ApiFinanceRow.fromJson(_map(e))),
        targetMarginPct: _int(j['targetMarginPct']),
      );

  final int cost;
  final int gross;
  final int revenue;
  final List<ApiFinanceRow> rows;
  final int targetMarginPct;
}

class ApiHistoryRow {
  const ApiHistoryRow({this.actorId, this.createdAt, this.id, this.note, this.requestId, this.status});

  factory ApiHistoryRow.fromJson(Map<String, dynamic> j) => ApiHistoryRow(
        actorId: j['actorId'] == null ? null : _int(j['actorId']),
        createdAt: j['createdAt']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        note: j['note']?.toString(),
        requestId: j['requestId'] == null ? null : _int(j['requestId']),
        status: j['status']?.toString(),
      );

  final int? actorId;
  final String? createdAt;
  final int? id;
  final String? note;
  final int? requestId;
  final String? status;
}

class ApiIdentityUserResponse {
  const ApiIdentityUserResponse({this.bio, this.country, this.createdAt, this.email, this.id, this.name, this.notifPrefs, this.organization, required this.permissions, this.phone, this.role, this.status, this.zone});

  factory ApiIdentityUserResponse.fromJson(Map<String, dynamic> j) => ApiIdentityUserResponse(
        bio: j['bio']?.toString(),
        country: j['country']?.toString(),
        createdAt: j['createdAt']?.toString(),
        email: j['email']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        name: j['name']?.toString(),
        notifPrefs: j['notifPrefs'] is Map ? ApiNotificationPreferences.fromJson(_map(j['notifPrefs'])) : null,
        organization: j['organization']?.toString(),
        permissions: _list(j['permissions'], (e) => _str(e)),
        phone: j['phone']?.toString(),
        role: j['role']?.toString(),
        status: j['status']?.toString(),
        zone: j['zone']?.toString(),
      );

  final String? bio;
  final String? country;
  final String? createdAt;
  final String? email;
  final int? id;
  final String? name;
  final ApiNotificationPreferences? notifPrefs;
  final String? organization;
  final List<String> permissions;
  final String? phone;
  final String? role;
  final String? status;
  final String? zone;
}

class ApiInstallmentRow {
  const ApiInstallmentRow({required this.amount, this.dueDate, this.id, this.notifiedAt, this.requestId, required this.seq, this.status});

  factory ApiInstallmentRow.fromJson(Map<String, dynamic> j) => ApiInstallmentRow(
        amount: _int(j['amount']),
        dueDate: j['dueDate']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        notifiedAt: j['notifiedAt']?.toString(),
        requestId: j['requestId'] == null ? null : _int(j['requestId']),
        seq: _int(j['seq']),
        status: j['status']?.toString(),
      );

  final int amount;
  final String? dueDate;
  final int? id;
  final String? notifiedAt;
  final int? requestId;
  final int seq;
  final String? status;
}

class ApiItemRow {
  const ApiItemRow({this.catalogItemId, required this.costEstimate, this.id, this.label, required this.quantity, this.requestId, this.target, required this.unitPrice});

  factory ApiItemRow.fromJson(Map<String, dynamic> j) => ApiItemRow(
        catalogItemId: j['catalogItemId'] == null ? null : _int(j['catalogItemId']),
        costEstimate: _int(j['costEstimate']),
        id: j['id'] == null ? null : _int(j['id']),
        label: j['label']?.toString(),
        quantity: _int(j['quantity']),
        requestId: j['requestId'] == null ? null : _int(j['requestId']),
        target: j['target']?.toString(),
        unitPrice: _int(j['unitPrice']),
      );

  final int? catalogItemId;
  final int costEstimate;
  final int? id;
  final String? label;
  final int quantity;
  final int? requestId;
  final String? target;
  final int unitPrice;
}

class ApiJsonNode {
  const ApiJsonNode();
  factory ApiJsonNode.fromJson(Map<String, dynamic> _) => const ApiJsonNode();
}

class ApiKpi {
  const ApiKpi({required this.acceptanceRate, required this.accepted, required this.amount, required this.avgDays, required this.documented, required this.late, required this.realizationRate, required this.realized, required this.received, required this.rejected});

  factory ApiKpi.fromJson(Map<String, dynamic> j) => ApiKpi(
        acceptanceRate: _dbl(j['acceptanceRate']),
        accepted: _int(j['accepted']),
        amount: _int(j['amount']),
        avgDays: _dbl(j['avgDays']),
        documented: _int(j['documented']),
        late: _int(j['late']),
        realizationRate: _dbl(j['realizationRate']),
        realized: _int(j['realized']),
        received: _int(j['received']),
        rejected: _int(j['rejected']),
      );

  final double acceptanceRate;
  final int accepted;
  final int amount;
  final double avgDays;
  final int documented;
  final int late;
  final double realizationRate;
  final int realized;
  final int received;
  final int rejected;
}

class ApiLevel {
  const ApiLevel({this.key, this.label, required this.min});

  factory ApiLevel.fromJson(Map<String, dynamic> j) => ApiLevel(
        key: j['key']?.toString(),
        label: j['label']?.toString(),
        min: _int(j['min']),
      );

  final String? key;
  final String? label;
  final int min;
}

class ApiLinkBeneficiary {
  const ApiLinkBeneficiary({this.beneficiaryId});

  factory ApiLinkBeneficiary.fromJson(Map<String, dynamic> j) => ApiLinkBeneficiary(
        beneficiaryId: j['beneficiaryId'] == null ? null : _int(j['beneficiaryId']),
      );

  final int? beneficiaryId;
}

class ApiListing {
  const ApiListing({this.createdAt, this.description, this.endsAt, required this.goalAmount, this.id, required this.menaSupport, this.ownerId, required this.raisedAmount, this.rejectionReason, this.status, this.title, this.type, this.zone});

  factory ApiListing.fromJson(Map<String, dynamic> j) => ApiListing(
        createdAt: j['createdAt']?.toString(),
        description: j['description']?.toString(),
        endsAt: j['endsAt']?.toString(),
        goalAmount: _int(j['goalAmount']),
        id: j['id'] == null ? null : _int(j['id']),
        menaSupport: _int(j['menaSupport']),
        ownerId: j['ownerId'] == null ? null : _int(j['ownerId']),
        raisedAmount: _int(j['raisedAmount']),
        rejectionReason: j['rejectionReason']?.toString(),
        status: j['status']?.toString(),
        title: j['title']?.toString(),
        type: j['type']?.toString(),
        zone: j['zone']?.toString(),
      );

  final String? createdAt;
  final String? description;
  final String? endsAt;
  final int goalAmount;
  final int? id;
  final int menaSupport;
  final int? ownerId;
  final int raisedAmount;
  final String? rejectionReason;
  final String? status;
  final String? title;
  final String? type;
  final String? zone;
}

class ApiListingView {
  const ApiListingView({required this.confirmedContributions, this.listing, this.organization, this.ownerName});

  factory ApiListingView.fromJson(Map<String, dynamic> j) => ApiListingView(
        confirmedContributions: _int(j['confirmedContributions']),
        listing: j['listing'] is Map ? ApiListing.fromJson(_map(j['listing'])) : null,
        organization: j['organization']?.toString(),
        ownerName: j['ownerName']?.toString(),
      );

  final int confirmedContributions;
  final ApiListing? listing;
  final String? organization;
  final String? ownerName;
}

class ApiManagedCatalogItemResponse {
  const ApiManagedCatalogItemResponse({required this.active, this.category, required this.costEstimate, this.createdAt, this.description, this.emoji, this.id, this.kind, this.slug, this.title, this.unit, required this.unitPrice});

  factory ApiManagedCatalogItemResponse.fromJson(Map<String, dynamic> j) => ApiManagedCatalogItemResponse(
        active: j['active'] == true,
        category: j['category']?.toString(),
        costEstimate: _int(j['costEstimate']),
        createdAt: j['createdAt']?.toString(),
        description: j['description']?.toString(),
        emoji: j['emoji']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        kind: j['kind']?.toString(),
        slug: j['slug']?.toString(),
        title: j['title']?.toString(),
        unit: j['unit']?.toString(),
        unitPrice: _int(j['unitPrice']),
      );

  final bool active;
  final String? category;
  final int costEstimate;
  final String? createdAt;
  final String? description;
  final String? emoji;
  final int? id;
  final String? kind;
  final String? slug;
  final String? title;
  final String? unit;
  final int unitPrice;
}

class ApiMargin {
  const ApiMargin({required this.cost, required this.fees, required this.gross, required this.pct});

  factory ApiMargin.fromJson(Map<String, dynamic> j) => ApiMargin(
        cost: _int(j['cost']),
        fees: _int(j['fees']),
        gross: _int(j['gross']),
        pct: _dbl(j['pct']),
      );

  final int cost;
  final int fees;
  final int gross;
  final double pct;
}

class ApiMissionRequestInfo {
  const ApiMissionRequestInfo({this.annexServices, this.id, this.ref, this.targetName, this.targetZone, this.title});

  factory ApiMissionRequestInfo.fromJson(Map<String, dynamic> j) => ApiMissionRequestInfo(
        annexServices: j['annexServices'] is Map ? _map(j['annexServices']) : null,
        id: j['id'] == null ? null : _int(j['id']),
        ref: j['ref']?.toString(),
        targetName: j['targetName']?.toString(),
        targetZone: j['targetZone']?.toString(),
        title: j['title']?.toString(),
      );

  final Map<String, dynamic>? annexServices;
  final int? id;
  final String? ref;
  final String? targetName;
  final String? targetZone;
  final String? title;
}

class ApiMissionRow {
  const ApiMissionRow({this.compensationMode, this.completedAt, this.createdAt, this.createdBy, this.dueDate, this.exceptionReason, this.exchangeDescription, this.exchangeStatus, this.id, this.instructions, required this.late, required this.providerFee, this.providerId, this.reassignedFrom, this.refusalReason, this.requestId, this.status, this.title, this.updatedAt, this.validatedAt});

  factory ApiMissionRow.fromJson(Map<String, dynamic> j) => ApiMissionRow(
        compensationMode: j['compensationMode']?.toString(),
        completedAt: j['completedAt']?.toString(),
        createdAt: j['createdAt']?.toString(),
        createdBy: j['createdBy'] == null ? null : _int(j['createdBy']),
        dueDate: j['dueDate']?.toString(),
        exceptionReason: j['exceptionReason']?.toString(),
        exchangeDescription: j['exchangeDescription']?.toString(),
        exchangeStatus: j['exchangeStatus']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        instructions: j['instructions']?.toString(),
        late: j['late'] == true,
        providerFee: _int(j['providerFee']),
        providerId: j['providerId'] == null ? null : _int(j['providerId']),
        reassignedFrom: j['reassignedFrom'] == null ? null : _int(j['reassignedFrom']),
        refusalReason: j['refusalReason']?.toString(),
        requestId: j['requestId'] == null ? null : _int(j['requestId']),
        status: j['status']?.toString(),
        title: j['title']?.toString(),
        updatedAt: j['updatedAt']?.toString(),
        validatedAt: j['validatedAt']?.toString(),
      );

  final String? compensationMode;
  final String? completedAt;
  final String? createdAt;
  final int? createdBy;
  final String? dueDate;
  final String? exceptionReason;
  final String? exchangeDescription;
  final String? exchangeStatus;
  final int? id;
  final String? instructions;
  final bool late;
  final int providerFee;
  final int? providerId;
  final int? reassignedFrom;
  final String? refusalReason;
  final int? requestId;
  final String? status;
  final String? title;
  final String? updatedAt;
  final String? validatedAt;
}

class ApiMissionSummary {
  const ApiMissionSummary({this.dueDate, this.id, this.providerName, this.status, this.title, this.validatedAt});

  factory ApiMissionSummary.fromJson(Map<String, dynamic> j) => ApiMissionSummary(
        dueDate: j['dueDate']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        providerName: j['providerName']?.toString(),
        status: j['status']?.toString(),
        title: j['title']?.toString(),
        validatedAt: j['validatedAt']?.toString(),
      );

  final String? dueDate;
  final int? id;
  final String? providerName;
  final String? status;
  final String? title;
  final String? validatedAt;
}

class ApiModerate {
  const ApiModerate({this.decision, this.reason});

  factory ApiModerate.fromJson(Map<String, dynamic> j) => ApiModerate(
        decision: j['decision']?.toString(),
        reason: j['reason']?.toString(),
      );

  final String? decision;
  final String? reason;
}

class ApiMyProvider {
  const ApiMyProvider({this.profile, required this.reviewCount, required this.reviews, required this.services});

  factory ApiMyProvider.fromJson(Map<String, dynamic> j) => ApiMyProvider(
        profile: j['profile'] is Map ? ApiProfile.fromJson(_map(j['profile'])) : null,
        reviewCount: _int(j['reviewCount']),
        reviews: _list(j['reviews'], (e) => ApiReview.fromJson(_map(e))),
        services: _list(j['services'], (e) => ApiService.fromJson(_map(e))),
      );

  final ApiProfile? profile;
  final int reviewCount;
  final List<ApiReview> reviews;
  final List<ApiService> services;
}

class ApiMyRewards {
  const ApiMyRewards({required this.badges, required this.balance, required this.conversionEnabled, required this.earned, this.level, required this.levels, this.next, required this.transactions, required this.transferEnabled});

  factory ApiMyRewards.fromJson(Map<String, dynamic> j) => ApiMyRewards(
        badges: _list(j['badges'], (e) => ApiBadgeView.fromJson(_map(e))),
        balance: _int(j['balance']),
        conversionEnabled: j['conversionEnabled'] == true,
        earned: _int(j['earned']),
        level: j['level'] is Map ? ApiLevel.fromJson(_map(j['level'])) : null,
        levels: _list(j['levels'], (e) => ApiLevel.fromJson(_map(e))),
        next: j['next'] is Map ? ApiLevel.fromJson(_map(j['next'])) : null,
        transactions: _list(j['transactions'], (e) => ApiTransactionView.fromJson(_map(e))),
        transferEnabled: j['transferEnabled'] == true,
      );

  final List<ApiBadgeView> badges;
  final int balance;
  final bool conversionEnabled;
  final int earned;
  final ApiLevel? level;
  final List<ApiLevel> levels;
  final ApiLevel? next;
  final List<ApiTransactionView> transactions;
  final bool transferEnabled;
}

class ApiNotificationPreferences {
  const ApiNotificationPreferences({required this.email, required this.inapp, required this.sms});

  factory ApiNotificationPreferences.fromJson(Map<String, dynamic> j) => ApiNotificationPreferences(
        email: j['email'] == true,
        inapp: j['inapp'] == true,
        sms: j['sms'] == true,
      );

  final bool email;
  final bool inapp;
  final bool sms;
}

class ApiNotificationView {
  const ApiNotificationView({this.body, this.createdAt, this.id, this.link, this.readAt, this.title, this.type});

  factory ApiNotificationView.fromJson(Map<String, dynamic> j) => ApiNotificationView(
        body: j['body']?.toString(),
        createdAt: j['createdAt']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        link: j['link']?.toString(),
        readAt: j['readAt']?.toString(),
        title: j['title']?.toString(),
        type: j['type']?.toString(),
      );

  final String? body;
  final String? createdAt;
  final int? id;
  final String? link;
  final String? readAt;
  final String? title;
  final String? type;
}

class ApiOrder {
  const ApiOrder({this.agreedPrice, this.clientId, this.compensationMode, this.createdAt, this.description, this.exchangeDescription, this.id, this.offerId, required this.proposedPrice, this.providerId, this.status, this.title, this.updatedAt, this.zone});

  factory ApiOrder.fromJson(Map<String, dynamic> j) => ApiOrder(
        agreedPrice: j['agreedPrice'] == null ? null : _int(j['agreedPrice']),
        clientId: j['clientId'] == null ? null : _int(j['clientId']),
        compensationMode: j['compensationMode']?.toString(),
        createdAt: j['createdAt']?.toString(),
        description: j['description']?.toString(),
        exchangeDescription: j['exchangeDescription']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        offerId: j['offerId'] == null ? null : _int(j['offerId']),
        proposedPrice: _int(j['proposedPrice']),
        providerId: j['providerId'] == null ? null : _int(j['providerId']),
        status: j['status']?.toString(),
        title: j['title']?.toString(),
        updatedAt: j['updatedAt']?.toString(),
        zone: j['zone']?.toString(),
      );

  final int? agreedPrice;
  final int? clientId;
  final String? compensationMode;
  final String? createdAt;
  final String? description;
  final String? exchangeDescription;
  final int? id;
  final int? offerId;
  final int proposedPrice;
  final int? providerId;
  final String? status;
  final String? title;
  final String? updatedAt;
  final String? zone;
}

class ApiOrderDetail {
  const ApiOrderDetail({this.clientName, required this.iAmProvider, required this.messages, this.order, required this.payments, this.providerName});

  factory ApiOrderDetail.fromJson(Map<String, dynamic> j) => ApiOrderDetail(
        clientName: j['clientName']?.toString(),
        iAmProvider: j['iAmProvider'] == true,
        messages: _list(j['messages'], (e) => ApiServiceMessageView.fromJson(_map(e))),
        order: j['order'] is Map ? ApiOrder.fromJson(_map(j['order'])) : null,
        payments: _list(j['payments'], (e) => ApiServicePayment.fromJson(_map(e))),
        providerName: j['providerName']?.toString(),
      );

  final String? clientName;
  final bool iAmProvider;
  final List<ApiServiceMessageView> messages;
  final ApiOrder? order;
  final List<ApiServicePayment> payments;
  final String? providerName;
}

class ApiOrderView {
  const ApiOrderView({this.clientName, required this.iAmProvider, this.order, this.providerName});

  factory ApiOrderView.fromJson(Map<String, dynamic> j) => ApiOrderView(
        clientName: j['clientName']?.toString(),
        iAmProvider: j['iAmProvider'] == true,
        order: j['order'] is Map ? ApiOrder.fromJson(_map(j['order'])) : null,
        providerName: j['providerName']?.toString(),
      );

  final String? clientName;
  final bool iAmProvider;
  final ApiOrder? order;
  final String? providerName;
}

class ApiOtpRequest {
  const ApiOtpRequest({this.mode, this.name, this.phone, this.role});

  factory ApiOtpRequest.fromJson(Map<String, dynamic> j) => ApiOtpRequest(
        mode: j['mode']?.toString(),
        name: j['name']?.toString(),
        phone: j['phone']?.toString(),
        role: j['role']?.toString(),
      );

  final String? mode;
  final String? name;
  final String? phone;
  final String? role;
}

class ApiOtpResponse {
  const ApiOtpResponse({this.devCode, this.error, required this.ok});

  factory ApiOtpResponse.fromJson(Map<String, dynamic> j) => ApiOtpResponse(
        devCode: j['devCode']?.toString(),
        error: j['error']?.toString(),
        ok: j['ok'] == true,
      );

  final String? devCode;
  final String? error;
  final bool ok;
}

class ApiOtpVerification {
  const ApiOtpVerification({this.code, this.phone});

  factory ApiOtpVerification.fromJson(Map<String, dynamic> j) => ApiOtpVerification(
        code: j['code']?.toString(),
        phone: j['phone']?.toString(),
      );

  final String? code;
  final String? phone;
}

class ApiPartnerDashboard {
  const ApiPartnerDashboard({required this.pending, required this.published, required this.raised, required this.total});

  factory ApiPartnerDashboard.fromJson(Map<String, dynamic> j) => ApiPartnerDashboard(
        pending: _int(j['pending']),
        published: _int(j['published']),
        raised: _int(j['raised']),
        total: _int(j['total']),
      );

  final int pending;
  final int published;
  final int raised;
  final int total;
}

class ApiPaymentRow {
  const ApiPaymentRow({required this.amountPaid, required this.amountPlanned, this.amountValidated, this.createdAt, this.direction, this.dueDate, this.exchangeDescription, this.exchangeStatus, required this.hasReceipt, this.id, this.installmentId, this.kind, this.method, this.missionId, this.mode, this.note, this.paidAt, this.receiptName, this.reference, required this.remaining, this.requestId, this.serviceOrderId, this.status, this.userId});

  factory ApiPaymentRow.fromJson(Map<String, dynamic> j) => ApiPaymentRow(
        amountPaid: _int(j['amountPaid']),
        amountPlanned: _int(j['amountPlanned']),
        amountValidated: j['amountValidated'] == null ? null : _int(j['amountValidated']),
        createdAt: j['createdAt']?.toString(),
        direction: j['direction']?.toString(),
        dueDate: j['dueDate']?.toString(),
        exchangeDescription: j['exchangeDescription']?.toString(),
        exchangeStatus: j['exchangeStatus']?.toString(),
        hasReceipt: j['hasReceipt'] == true,
        id: j['id'] == null ? null : _int(j['id']),
        installmentId: j['installmentId'] == null ? null : _int(j['installmentId']),
        kind: j['kind']?.toString(),
        method: j['method']?.toString(),
        missionId: j['missionId'] == null ? null : _int(j['missionId']),
        mode: j['mode']?.toString(),
        note: j['note']?.toString(),
        paidAt: j['paidAt']?.toString(),
        receiptName: j['receiptName']?.toString(),
        reference: j['reference']?.toString(),
        remaining: _int(j['remaining']),
        requestId: j['requestId'] == null ? null : _int(j['requestId']),
        serviceOrderId: j['serviceOrderId'] == null ? null : _int(j['serviceOrderId']),
        status: j['status']?.toString(),
        userId: j['userId'] == null ? null : _int(j['userId']),
      );

  final int amountPaid;
  final int amountPlanned;
  final int? amountValidated;
  final String? createdAt;
  final String? direction;
  final String? dueDate;
  final String? exchangeDescription;
  final String? exchangeStatus;
  final bool hasReceipt;
  final int? id;
  final int? installmentId;
  final String? kind;
  final String? method;
  final int? missionId;
  final String? mode;
  final String? note;
  final String? paidAt;
  final String? receiptName;
  final String? reference;
  final int remaining;
  final int? requestId;
  final int? serviceOrderId;
  final String? status;
  final int? userId;
}

class ApiPostMessage {
  const ApiPostMessage({this.body, this.price});

  factory ApiPostMessage.fromJson(Map<String, dynamic> j) => ApiPostMessage(
        body: j['body']?.toString(),
        price: j['price'] == null ? null : _int(j['price']),
      );

  final String? body;
  final int? price;
}

class ApiProfile {
  const ApiProfile({required this.available, this.category, this.description, this.userId, required this.verified, this.zone});

  factory ApiProfile.fromJson(Map<String, dynamic> j) => ApiProfile(
        available: j['available'] == true,
        category: j['category']?.toString(),
        description: j['description']?.toString(),
        userId: j['userId'] == null ? null : _int(j['userId']),
        verified: j['verified'] == true,
        zone: j['zone']?.toString(),
      );

  final bool available;
  final String? category;
  final String? description;
  final int? userId;
  final bool verified;
  final String? zone;
}

class ApiProfileRequest {
  const ApiProfileRequest({required this.available, this.category, this.description, this.zone});

  factory ApiProfileRequest.fromJson(Map<String, dynamic> j) => ApiProfileRequest(
        available: j['available'] == true,
        category: j['category']?.toString(),
        description: j['description']?.toString(),
        zone: j['zone']?.toString(),
      );

  final bool available;
  final String? category;
  final String? description;
  final String? zone;
}

class ApiProfileUpdate {
  const ApiProfileUpdate({this.bio, this.email, this.name, required this.notifEmail, required this.notifInapp, required this.notifSms, this.organization, this.zone});

  factory ApiProfileUpdate.fromJson(Map<String, dynamic> j) => ApiProfileUpdate(
        bio: j['bio']?.toString(),
        email: j['email']?.toString(),
        name: j['name']?.toString(),
        notifEmail: j['notifEmail'] == true,
        notifInapp: j['notifInapp'] == true,
        notifSms: j['notifSms'] == true,
        organization: j['organization']?.toString(),
        zone: j['zone']?.toString(),
      );

  final String? bio;
  final String? email;
  final String? name;
  final bool notifEmail;
  final bool notifInapp;
  final bool notifSms;
  final String? organization;
  final String? zone;
}

class ApiProofReview {
  const ApiProofReview({this.decision, required this.hide, this.reason});

  factory ApiProofReview.fromJson(Map<String, dynamic> j) => ApiProofReview(
        decision: j['decision']?.toString(),
        hide: j['hide'] == true,
        reason: j['reason']?.toString(),
      );

  final String? decision;
  final bool hide;
  final String? reason;
}

class ApiProofRow {
  const ApiProofRow({this.caption, this.createdAt, this.fileName, this.id, this.mime, this.missionId, this.rejectionReason, this.requestId, this.reviewedAt, this.reviewedBy, required this.size, this.status, this.type, this.uploadedBy, required this.visibleToDonor});

  factory ApiProofRow.fromJson(Map<String, dynamic> j) => ApiProofRow(
        caption: j['caption']?.toString(),
        createdAt: j['createdAt']?.toString(),
        fileName: j['fileName']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        mime: j['mime']?.toString(),
        missionId: j['missionId'] == null ? null : _int(j['missionId']),
        rejectionReason: j['rejectionReason']?.toString(),
        requestId: j['requestId'] == null ? null : _int(j['requestId']),
        reviewedAt: j['reviewedAt']?.toString(),
        reviewedBy: j['reviewedBy'] == null ? null : _int(j['reviewedBy']),
        size: _int(j['size']),
        status: j['status']?.toString(),
        type: j['type']?.toString(),
        uploadedBy: j['uploadedBy'] == null ? null : _int(j['uploadedBy']),
        visibleToDonor: j['visibleToDonor'] == true,
      );

  final String? caption;
  final String? createdAt;
  final String? fileName;
  final int? id;
  final String? mime;
  final int? missionId;
  final String? rejectionReason;
  final int? requestId;
  final String? reviewedAt;
  final int? reviewedBy;
  final int size;
  final String? status;
  final String? type;
  final int? uploadedBy;
  final bool visibleToDonor;
}

class ApiProviderCard {
  const ApiProviderCard({required this.available, this.category, this.description, this.id, this.name, this.phone, required this.rating, required this.reviews, required this.services, required this.verified, this.zone});

  factory ApiProviderCard.fromJson(Map<String, dynamic> j) => ApiProviderCard(
        available: j['available'] == true,
        category: j['category']?.toString(),
        description: j['description']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        name: j['name']?.toString(),
        phone: j['phone']?.toString(),
        rating: _dbl(j['rating']),
        reviews: _int(j['reviews']),
        services: _list(j['services'], (e) => ApiService.fromJson(_map(e))),
        verified: j['verified'] == true,
        zone: j['zone']?.toString(),
      );

  final bool available;
  final String? category;
  final String? description;
  final int? id;
  final String? name;
  final String? phone;
  final double rating;
  final int reviews;
  final List<ApiService> services;
  final bool verified;
  final String? zone;
}

class ApiProviderDashboard {
  const ApiProviderDashboard({required this.accepted, required this.completed, required this.inProgress, required this.proposals, required this.proposed, required this.validated});

  factory ApiProviderDashboard.fromJson(Map<String, dynamic> j) => ApiProviderDashboard(
        accepted: _int(j['accepted']),
        completed: _int(j['completed']),
        inProgress: _int(j['inProgress']),
        proposals: _list(j['proposals'], (e) => ApiMissionRow.fromJson(_map(e))),
        proposed: _int(j['proposed']),
        validated: _int(j['validated']),
      );

  final int accepted;
  final int completed;
  final int inProgress;
  final List<ApiMissionRow> proposals;
  final int proposed;
  final int validated;
}

class ApiProviderMissionDetail {
  const ApiProviderMissionDetail({this.beneficiary, required this.items, this.mission, required this.proofs, this.request});

  factory ApiProviderMissionDetail.fromJson(Map<String, dynamic> j) => ApiProviderMissionDetail(
        beneficiary: j['beneficiary'] is Map ? ApiBeneficiaryContact.fromJson(_map(j['beneficiary'])) : null,
        items: _list(j['items'], (e) => ApiItemRow.fromJson(_map(e))),
        mission: j['mission'] is Map ? ApiMissionRow.fromJson(_map(j['mission'])) : null,
        proofs: _list(j['proofs'], (e) => ApiProofRow.fromJson(_map(e))),
        request: j['request'] is Map ? ApiMissionRequestInfo.fromJson(_map(j['request'])) : null,
      );

  final ApiBeneficiaryContact? beneficiary;
  final List<ApiItemRow> items;
  final ApiMissionRow? mission;
  final List<ApiProofRow> proofs;
  final ApiMissionRequestInfo? request;
}

class ApiProviderOption {
  const ApiProviderOption({this.category, this.id, this.name});

  factory ApiProviderOption.fromJson(Map<String, dynamic> j) => ApiProviderOption(
        category: j['category']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        name: j['name']?.toString(),
      );

  final String? category;
  final int? id;
  final String? name;
}

class ApiPublicListing {
  const ApiPublicListing({this.description, this.endsAt, required this.goalAmount, this.id, required this.menaSupport, this.organization, required this.raisedAmount, this.title, this.type, this.zone});

  factory ApiPublicListing.fromJson(Map<String, dynamic> j) => ApiPublicListing(
        description: j['description']?.toString(),
        endsAt: j['endsAt']?.toString(),
        goalAmount: _int(j['goalAmount']),
        id: j['id'] == null ? null : _int(j['id']),
        menaSupport: _int(j['menaSupport']),
        organization: j['organization']?.toString(),
        raisedAmount: _int(j['raisedAmount']),
        title: j['title']?.toString(),
        type: j['type']?.toString(),
        zone: j['zone']?.toString(),
      );

  final String? description;
  final String? endsAt;
  final int goalAmount;
  final int? id;
  final int menaSupport;
  final String? organization;
  final int raisedAmount;
  final String? title;
  final String? type;
  final String? zone;
}

class ApiPublicPartner {
  const ApiPublicPartner({this.id, this.name, this.organization, this.zone});

  factory ApiPublicPartner.fromJson(Map<String, dynamic> j) => ApiPublicPartner(
        id: j['id'] == null ? null : _int(j['id']),
        name: j['name']?.toString(),
        organization: j['organization']?.toString(),
        zone: j['zone']?.toString(),
      );

  final int? id;
  final String? name;
  final String? organization;
  final String? zone;
}

class ApiReason {
  const ApiReason({this.reason});

  factory ApiReason.fromJson(Map<String, dynamic> j) => ApiReason(
        reason: j['reason']?.toString(),
      );

  final String? reason;
}

class ApiReassign {
  const ApiReassign({this.providerId});

  factory ApiReassign.fromJson(Map<String, dynamic> j) => ApiReassign(
        providerId: j['providerId'] == null ? null : _int(j['providerId']),
      );

  final int? providerId;
}

class ApiReceivedGift {
  const ApiReceivedGift({this.createdAt, this.id, this.status, this.title});

  factory ApiReceivedGift.fromJson(Map<String, dynamic> j) => ApiReceivedGift(
        createdAt: j['createdAt']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        status: j['status']?.toString(),
        title: j['title']?.toString(),
      );

  final String? createdAt;
  final int? id;
  final String? status;
  final String? title;
}

class ApiRequestRow {
  const ApiRequestRow({required this.amountPerOccurrence, required this.annexCost, this.annexServices, this.beneficiaryId, this.category, this.closedAt, this.completedAt, this.createdAt, this.description, required this.executionBudget, this.id, this.infoRequest, this.kind, required this.occurrences, required this.otherFees, this.partnerId, this.paymentMode, this.recurrence, this.ref, this.rejectionReason, this.startDate, this.status, this.targetName, this.targetZone, this.title, required this.totalAmount, this.updatedAt, this.userId});

  factory ApiRequestRow.fromJson(Map<String, dynamic> j) => ApiRequestRow(
        amountPerOccurrence: _int(j['amountPerOccurrence']),
        annexCost: _int(j['annexCost']),
        annexServices: j['annexServices'] is Map ? _map(j['annexServices']) : null,
        beneficiaryId: j['beneficiaryId'] == null ? null : _int(j['beneficiaryId']),
        category: j['category']?.toString(),
        closedAt: j['closedAt']?.toString(),
        completedAt: j['completedAt']?.toString(),
        createdAt: j['createdAt']?.toString(),
        description: j['description']?.toString(),
        executionBudget: _int(j['executionBudget']),
        id: j['id'] == null ? null : _int(j['id']),
        infoRequest: j['infoRequest']?.toString(),
        kind: j['kind']?.toString(),
        occurrences: _int(j['occurrences']),
        otherFees: _int(j['otherFees']),
        partnerId: j['partnerId'] == null ? null : _int(j['partnerId']),
        paymentMode: j['paymentMode']?.toString(),
        recurrence: j['recurrence']?.toString(),
        ref: j['ref']?.toString(),
        rejectionReason: j['rejectionReason']?.toString(),
        startDate: j['startDate']?.toString(),
        status: j['status']?.toString(),
        targetName: j['targetName']?.toString(),
        targetZone: j['targetZone']?.toString(),
        title: j['title']?.toString(),
        totalAmount: _int(j['totalAmount']),
        updatedAt: j['updatedAt']?.toString(),
        userId: j['userId'] == null ? null : _int(j['userId']),
      );

  final int amountPerOccurrence;
  final int annexCost;
  final Map<String, dynamic>? annexServices;
  final int? beneficiaryId;
  final String? category;
  final String? closedAt;
  final String? completedAt;
  final String? createdAt;
  final String? description;
  final int executionBudget;
  final int? id;
  final String? infoRequest;
  final String? kind;
  final int occurrences;
  final int otherFees;
  final int? partnerId;
  final String? paymentMode;
  final String? recurrence;
  final String? ref;
  final String? rejectionReason;
  final String? startDate;
  final String? status;
  final String? targetName;
  final String? targetZone;
  final String? title;
  final int totalAmount;
  final String? updatedAt;
  final int? userId;
}

class ApiRespond {
  const ApiRespond({this.decision, this.reason});

  factory ApiRespond.fromJson(Map<String, dynamic> j) => ApiRespond(
        decision: j['decision']?.toString(),
        reason: j['reason']?.toString(),
      );

  final String? decision;
  final String? reason;
}

class ApiReview {
  const ApiReview({this.comment, this.createdAt, this.fromUserId, this.id, this.kind, required this.rating, this.refId, this.toUserId});

  factory ApiReview.fromJson(Map<String, dynamic> j) => ApiReview(
        comment: j['comment']?.toString(),
        createdAt: j['createdAt']?.toString(),
        fromUserId: j['fromUserId'] == null ? null : _int(j['fromUserId']),
        id: j['id'] == null ? null : _int(j['id']),
        kind: j['kind']?.toString(),
        rating: _int(j['rating']),
        refId: j['refId'] == null ? null : _int(j['refId']),
        toUserId: j['toUserId'] == null ? null : _int(j['toUserId']),
      );

  final String? comment;
  final String? createdAt;
  final int? fromUserId;
  final int? id;
  final String? kind;
  final int rating;
  final int? refId;
  final int? toUserId;
}

class ApiReviewRequest {
  const ApiReviewRequest({this.comment, required this.rating});

  factory ApiReviewRequest.fromJson(Map<String, dynamic> j) => ApiReviewRequest(
        comment: j['comment']?.toString(),
        rating: _int(j['rating']),
      );

  final String? comment;
  final int rating;
}

class ApiRule {
  const ApiRule({required this.active, required this.amount, required this.dailyCap, this.eventKey, this.id, this.label});

  factory ApiRule.fromJson(Map<String, dynamic> j) => ApiRule(
        active: j['active'] == true,
        amount: _int(j['amount']),
        dailyCap: _int(j['dailyCap']),
        eventKey: j['eventKey']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        label: j['label']?.toString(),
      );

  final bool active;
  final int amount;
  final int dailyCap;
  final String? eventKey;
  final int? id;
  final String? label;
}

class ApiRuleRequest {
  const ApiRuleRequest({required this.active, required this.amount, required this.dailyCap, this.eventKey, this.label});

  factory ApiRuleRequest.fromJson(Map<String, dynamic> j) => ApiRuleRequest(
        active: j['active'] == true,
        amount: _int(j['amount']),
        dailyCap: _int(j['dailyCap']),
        eventKey: j['eventKey']?.toString(),
        label: j['label']?.toString(),
      );

  final bool active;
  final int amount;
  final int dailyCap;
  final String? eventKey;
  final String? label;
}

class ApiSaveCatalogItemRequest {
  const ApiSaveCatalogItemRequest({required this.active, this.category, required this.costEstimate, this.description, this.emoji, this.title, this.unit, required this.unitPrice});

  factory ApiSaveCatalogItemRequest.fromJson(Map<String, dynamic> j) => ApiSaveCatalogItemRequest(
        active: j['active'] == true,
        category: j['category']?.toString(),
        costEstimate: _int(j['costEstimate']),
        description: j['description']?.toString(),
        emoji: j['emoji']?.toString(),
        title: j['title']?.toString(),
        unit: j['unit']?.toString(),
        unitPrice: _int(j['unitPrice']),
      );

  final bool active;
  final String? category;
  final int costEstimate;
  final String? description;
  final String? emoji;
  final String? title;
  final String? unit;
  final int unitPrice;
}

class ApiSaveSettingRequest {
  const ApiSaveSettingRequest({this.value});

  factory ApiSaveSettingRequest.fromJson(Map<String, dynamic> j) => ApiSaveSettingRequest(
        value: j['value'] is Map ? ApiJsonNode.fromJson(_map(j['value'])) : null,
      );

  final ApiJsonNode? value;
}

class ApiSaveTarget {
  const ApiSaveTarget({required this.active, this.description, this.label});

  factory ApiSaveTarget.fromJson(Map<String, dynamic> j) => ApiSaveTarget(
        active: j['active'] == true,
        description: j['description']?.toString(),
        label: j['label']?.toString(),
      );

  final bool active;
  final String? description;
  final String? label;
}

class ApiSchedule {
  const ApiSchedule({this.occurrences, this.paymentMode, this.recurrence, this.startDate});

  factory ApiSchedule.fromJson(Map<String, dynamic> j) => ApiSchedule(
        occurrences: j['occurrences'] == null ? null : _int(j['occurrences']),
        paymentMode: j['paymentMode']?.toString(),
        recurrence: j['recurrence']?.toString(),
        startDate: j['startDate']?.toString(),
      );

  final int? occurrences;
  final String? paymentMode;
  final String? recurrence;
  final String? startDate;
}

class ApiService {
  const ApiService({required this.active, this.category, this.description, this.id, this.label, this.providerId, this.unit, required this.unitCost});

  factory ApiService.fromJson(Map<String, dynamic> j) => ApiService(
        active: j['active'] == true,
        category: j['category']?.toString(),
        description: j['description']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        label: j['label']?.toString(),
        providerId: j['providerId'] == null ? null : _int(j['providerId']),
        unit: j['unit']?.toString(),
        unitCost: _int(j['unitCost']),
      );

  final bool active;
  final String? category;
  final String? description;
  final int? id;
  final String? label;
  final int? providerId;
  final String? unit;
  final int unitCost;
}

class ApiServiceMessage {
  const ApiServiceMessage({this.authorId, this.body, this.createdAt, this.id, this.orderId, this.price});

  factory ApiServiceMessage.fromJson(Map<String, dynamic> j) => ApiServiceMessage(
        authorId: j['authorId'] == null ? null : _int(j['authorId']),
        body: j['body']?.toString(),
        createdAt: j['createdAt']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        orderId: j['orderId'] == null ? null : _int(j['orderId']),
        price: j['price'] == null ? null : _int(j['price']),
      );

  final int? authorId;
  final String? body;
  final String? createdAt;
  final int? id;
  final int? orderId;
  final int? price;
}

class ApiServiceMessageView {
  const ApiServiceMessageView({this.authorName, this.message});

  factory ApiServiceMessageView.fromJson(Map<String, dynamic> j) => ApiServiceMessageView(
        authorName: j['authorName']?.toString(),
        message: j['message'] is Map ? ApiServiceMessage.fromJson(_map(j['message'])) : null,
      );

  final String? authorName;
  final ApiServiceMessage? message;
}

class ApiServicePayment {
  const ApiServicePayment({required this.amountPaid, required this.amountPlanned, this.exchangeStatus, this.id, this.mode, this.status});

  factory ApiServicePayment.fromJson(Map<String, dynamic> j) => ApiServicePayment(
        amountPaid: _int(j['amountPaid']),
        amountPlanned: _int(j['amountPlanned']),
        exchangeStatus: j['exchangeStatus']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        mode: j['mode']?.toString(),
        status: j['status']?.toString(),
      );

  final int amountPaid;
  final int amountPlanned;
  final String? exchangeStatus;
  final int? id;
  final String? mode;
  final String? status;
}

class ApiServiceRequest {
  const ApiServiceRequest({this.category, this.description, this.label, this.unit, required this.unitCost});

  factory ApiServiceRequest.fromJson(Map<String, dynamic> j) => ApiServiceRequest(
        category: j['category']?.toString(),
        description: j['description']?.toString(),
        label: j['label']?.toString(),
        unit: j['unit']?.toString(),
        unitCost: _int(j['unitCost']),
      );

  final String? category;
  final String? description;
  final String? label;
  final String? unit;
  final int unitCost;
}

class ApiSessionResponse {
  const ApiSessionResponse({this.accessToken, this.expiresAt, this.tokenType, this.user});

  factory ApiSessionResponse.fromJson(Map<String, dynamic> j) => ApiSessionResponse(
        accessToken: j['accessToken']?.toString(),
        expiresAt: j['expiresAt']?.toString(),
        tokenType: j['tokenType']?.toString(),
        user: j['user'] is Map ? ApiIdentityUserResponse.fromJson(_map(j['user'])) : null,
      );

  final String? accessToken;
  final String? expiresAt;
  final String? tokenType;
  final ApiIdentityUserResponse? user;
}

class ApiSetCatalogItemActiveRequest {
  const ApiSetCatalogItemActiveRequest({required this.active});

  factory ApiSetCatalogItemActiveRequest.fromJson(Map<String, dynamic> j) => ApiSetCatalogItemActiveRequest(
        active: j['active'] == true,
      );

  final bool active;
}

class ApiSetRoleRequest {
  const ApiSetRoleRequest({required this.permissions, this.role});

  factory ApiSetRoleRequest.fromJson(Map<String, dynamic> j) => ApiSetRoleRequest(
        permissions: _list(j['permissions'], (e) => _str(e)),
        role: j['role']?.toString(),
      );

  final List<String> permissions;
  final String? role;
}

class ApiSetStatusRequest {
  const ApiSetStatusRequest({this.status});

  factory ApiSetStatusRequest.fromJson(Map<String, dynamic> j) => ApiSetStatusRequest(
        status: j['status']?.toString(),
      );

  final String? status;
}

class ApiSettingView {
  const ApiSettingView({this.description, this.key, this.updatedAt, this.updatedBy, this.value});

  factory ApiSettingView.fromJson(Map<String, dynamic> j) => ApiSettingView(
        description: j['description']?.toString(),
        key: j['key']?.toString(),
        updatedAt: j['updatedAt']?.toString(),
        updatedBy: j['updatedBy'] == null ? null : _int(j['updatedBy']),
        value: j['value'] is Map ? ApiJsonNode.fromJson(_map(j['value'])) : null,
      );

  final String? description;
  final String? key;
  final String? updatedAt;
  final int? updatedBy;
  final ApiJsonNode? value;
}

class ApiStatRow {
  const ApiStatRow({required this.late, this.providers, this.request});

  factory ApiStatRow.fromJson(Map<String, dynamic> j) => ApiStatRow(
        late: j['late'] == true,
        providers: j['providers']?.toString(),
        request: j['request'] is Map ? ApiRequestRow.fromJson(_map(j['request'])) : null,
      );

  final bool late;
  final String? providers;
  final ApiRequestRow? request;
}

class ApiStats {
  const ApiStats({this.kpi, required this.rows});

  factory ApiStats.fromJson(Map<String, dynamic> j) => ApiStats(
        kpi: j['kpi'] is Map ? ApiKpi.fromJson(_map(j['kpi'])) : null,
        rows: _list(j['rows'], (e) => ApiStatRow.fromJson(_map(e))),
      );

  final ApiKpi? kpi;
  final List<ApiStatRow> rows;
}

class ApiStatus {
  const ApiStatus({this.label, this.tone});

  factory ApiStatus.fromJson(Map<String, dynamic> j) => ApiStatus(
        label: j['label']?.toString(),
        tone: j['tone']?.toString(),
      );

  final String? label;
  final String? tone;
}

class ApiSupport {
  const ApiSupport({required this.amount});

  factory ApiSupport.fromJson(Map<String, dynamic> j) => ApiSupport(
        amount: _int(j['amount']),
      );

  final int amount;
}

class ApiTarget {
  const ApiTarget({required this.active, this.description, this.id, this.label});

  factory ApiTarget.fromJson(Map<String, dynamic> j) => ApiTarget(
        active: j['active'] == true,
        description: j['description']?.toString(),
        id: j['id']?.toString(),
        label: j['label']?.toString(),
      );

  final bool active;
  final String? description;
  final String? id;
  final String? label;
}

class ApiTargetUsage {
  const ApiTargetUsage({required this.count, this.target});

  factory ApiTargetUsage.fromJson(Map<String, dynamic> j) => ApiTargetUsage(
        count: _int(j['count']),
        target: j['target']?.toString(),
      );

  final int count;
  final String? target;
}

class ApiTransactionView {
  const ApiTransactionView({required this.amount, this.createdAt, this.createdBy, this.eventKey, this.id, this.reason, this.status, this.type, this.userId});

  factory ApiTransactionView.fromJson(Map<String, dynamic> j) => ApiTransactionView(
        amount: _int(j['amount']),
        createdAt: j['createdAt']?.toString(),
        createdBy: j['createdBy'] == null ? null : _int(j['createdBy']),
        eventKey: j['eventKey']?.toString(),
        id: j['id'] == null ? null : _int(j['id']),
        reason: j['reason']?.toString(),
        status: j['status']?.toString(),
        type: j['type']?.toString(),
        userId: j['userId'] == null ? null : _int(j['userId']),
      );

  final int amount;
  final String? createdAt;
  final int? createdBy;
  final String? eventKey;
  final int? id;
  final String? reason;
  final String? status;
  final String? type;
  final int? userId;
}

class ApiTransferRequest {
  const ApiTransferRequest({required this.amount, this.phone});

  factory ApiTransferRequest.fromJson(Map<String, dynamic> j) => ApiTransferRequest(
        amount: _int(j['amount']),
        phone: j['phone']?.toString(),
      );

  final int amount;
  final String? phone;
}

class ApiTransition {
  const ApiTransition({this.action, this.comment, this.rating});

  factory ApiTransition.fromJson(Map<String, dynamic> j) => ApiTransition(
        action: j['action']?.toString(),
        comment: j['comment']?.toString(),
        rating: j['rating'] == null ? null : _int(j['rating']),
      );

  final String? action;
  final String? comment;
  final int? rating;
}

class ApiUserOption {
  const ApiUserOption({this.id, this.name, this.phone, this.role});

  factory ApiUserOption.fromJson(Map<String, dynamic> j) => ApiUserOption(
        id: j['id'] == null ? null : _int(j['id']),
        name: j['name']?.toString(),
        phone: j['phone']?.toString(),
        role: j['role']?.toString(),
      );

  final int? id;
  final String? name;
  final String? phone;
  final String? role;
}

class ApiVerifyRequest {
  const ApiVerifyRequest({required this.verified});

  factory ApiVerifyRequest.fromJson(Map<String, dynamic> j) => ApiVerifyRequest(
        verified: j['verified'] == true,
      );

  final bool verified;
}
