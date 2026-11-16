CREATE TABLE "audit_logs" (
	"id" serial PRIMARY KEY NOT NULL,
	"actor_id" integer,
	"actor_role" text,
	"action" text NOT NULL,
	"entity" text,
	"entity_id" text,
	"details" jsonb,
	"ip" text,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "beneficiaries" (
	"id" serial PRIMARY KEY NOT NULL,
	"full_name" text NOT NULL,
	"phone" text,
	"zone" text,
	"address" text,
	"needs" text,
	"notes" text,
	"consent" boolean DEFAULT false NOT NULL,
	"user_id" integer,
	"created_by" integer,
	"archived_at" timestamp with time zone,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "cart_items" (
	"id" serial PRIMARY KEY NOT NULL,
	"user_id" integer NOT NULL,
	"catalog_item_id" integer NOT NULL,
	"quantity" integer DEFAULT 1 NOT NULL,
	"target" text,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "catalog_items" (
	"id" serial PRIMARY KEY NOT NULL,
	"slug" text NOT NULL,
	"title" text NOT NULL,
	"category" text NOT NULL,
	"kind" text DEFAULT 'don' NOT NULL,
	"description" text,
	"unit_price" integer NOT NULL,
	"cost_estimate" integer DEFAULT 0 NOT NULL,
	"unit" text DEFAULT 'unité' NOT NULL,
	"emoji" text DEFAULT '🎁',
	"active" boolean DEFAULT true NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	CONSTRAINT "catalog_items_slug_unique" UNIQUE("slug")
);
--> statement-breakpoint
CREATE TABLE "contact_messages" (
	"id" serial PRIMARY KEY NOT NULL,
	"name" text NOT NULL,
	"email" text,
	"phone" text,
	"subject" text NOT NULL,
	"message" text NOT NULL,
	"handled" boolean DEFAULT false NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "dispute_messages" (
	"id" serial PRIMARY KEY NOT NULL,
	"dispute_id" integer NOT NULL,
	"author_id" integer NOT NULL,
	"body" text NOT NULL,
	"attachment_path" text,
	"attachment_name" text,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "disputes" (
	"id" serial PRIMARY KEY NOT NULL,
	"ref" text NOT NULL,
	"request_id" integer,
	"mission_id" integer,
	"opened_by" integer NOT NULL,
	"against_user_id" integer,
	"subject" text NOT NULL,
	"description" text,
	"status" text DEFAULT 'open' NOT NULL,
	"responsible_id" integer,
	"decision" text,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"closed_at" timestamp with time zone,
	CONSTRAINT "disputes_ref_unique" UNIQUE("ref")
);
--> statement-breakpoint
CREATE TABLE "installments" (
	"id" serial PRIMARY KEY NOT NULL,
	"request_id" integer NOT NULL,
	"seq" integer NOT NULL,
	"due_date" timestamp with time zone NOT NULL,
	"amount" integer NOT NULL,
	"status" text DEFAULT 'planned' NOT NULL,
	"notified_at" timestamp with time zone
);
--> statement-breakpoint
CREATE TABLE "listing_contributions" (
	"id" serial PRIMARY KEY NOT NULL,
	"listing_id" integer NOT NULL,
	"user_id" integer NOT NULL,
	"amount" integer NOT NULL,
	"method" text,
	"reference" text,
	"status" text DEFAULT 'declared' NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "listings" (
	"id" serial PRIMARY KEY NOT NULL,
	"owner_id" integer NOT NULL,
	"type" text NOT NULL,
	"title" text NOT NULL,
	"description" text,
	"zone" text,
	"goal_amount" integer DEFAULT 0 NOT NULL,
	"raised_amount" integer DEFAULT 0 NOT NULL,
	"mena_support" integer DEFAULT 0 NOT NULL,
	"ends_at" timestamp with time zone,
	"status" text DEFAULT 'pending' NOT NULL,
	"rejection_reason" text,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "missions" (
	"id" serial PRIMARY KEY NOT NULL,
	"request_id" integer NOT NULL,
	"provider_id" integer,
	"title" text NOT NULL,
	"instructions" text,
	"status" text DEFAULT 'proposed' NOT NULL,
	"refusal_reason" text,
	"due_date" timestamp with time zone,
	"provider_fee" integer DEFAULT 0 NOT NULL,
	"compensation_mode" text DEFAULT 'money' NOT NULL,
	"exchange_description" text,
	"exchange_status" text DEFAULT 'pending',
	"exception_reason" text,
	"reassigned_from" integer,
	"created_by" integer,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL,
	"completed_at" timestamp with time zone,
	"validated_at" timestamp with time zone
);
--> statement-breakpoint
CREATE TABLE "notifications" (
	"id" serial PRIMARY KEY NOT NULL,
	"user_id" integer NOT NULL,
	"type" text NOT NULL,
	"title" text NOT NULL,
	"body" text,
	"link" text,
	"read_at" timestamp with time zone,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "otp_codes" (
	"id" serial PRIMARY KEY NOT NULL,
	"phone" text NOT NULL,
	"code_hash" text NOT NULL,
	"meta" jsonb,
	"attempts" integer DEFAULT 0 NOT NULL,
	"expires_at" timestamp with time zone NOT NULL,
	"consumed_at" timestamp with time zone,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "payments" (
	"id" serial PRIMARY KEY NOT NULL,
	"request_id" integer,
	"mission_id" integer,
	"service_order_id" integer,
	"installment_id" integer,
	"user_id" integer NOT NULL,
	"direction" text NOT NULL,
	"kind" text DEFAULT 'don' NOT NULL,
	"mode" text DEFAULT 'money' NOT NULL,
	"method" text,
	"amount_planned" integer DEFAULT 0 NOT NULL,
	"amount_validated" integer,
	"amount_paid" integer DEFAULT 0 NOT NULL,
	"status" text DEFAULT 'planned' NOT NULL,
	"reference" text,
	"receipt_path" text,
	"receipt_name" text,
	"exchange_description" text,
	"exchange_status" text,
	"due_date" timestamp with time zone,
	"note" text,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"paid_at" timestamp with time zone
);
--> statement-breakpoint
CREATE TABLE "proofs" (
	"id" serial PRIMARY KEY NOT NULL,
	"request_id" integer NOT NULL,
	"mission_id" integer,
	"type" text NOT NULL,
	"file_path" text NOT NULL,
	"file_name" text NOT NULL,
	"mime" text NOT NULL,
	"size" integer DEFAULT 0 NOT NULL,
	"caption" text,
	"uploaded_by" integer NOT NULL,
	"status" text DEFAULT 'pending' NOT NULL,
	"rejection_reason" text,
	"reviewed_by" integer,
	"reviewed_at" timestamp with time zone,
	"visible_to_donor" boolean DEFAULT true NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "provider_profiles" (
	"user_id" integer PRIMARY KEY NOT NULL,
	"category" text DEFAULT 'autre' NOT NULL,
	"description" text,
	"zone" text,
	"available" boolean DEFAULT true NOT NULL,
	"verified" boolean DEFAULT false NOT NULL
);
--> statement-breakpoint
CREATE TABLE "provider_services" (
	"id" serial PRIMARY KEY NOT NULL,
	"provider_id" integer NOT NULL,
	"label" text NOT NULL,
	"category" text DEFAULT 'autre' NOT NULL,
	"unit_cost" integer DEFAULT 0 NOT NULL,
	"unit" text DEFAULT 'prestation' NOT NULL,
	"description" text,
	"active" boolean DEFAULT true NOT NULL
);
--> statement-breakpoint
CREATE TABLE "request_history" (
	"id" serial PRIMARY KEY NOT NULL,
	"request_id" integer NOT NULL,
	"status" text NOT NULL,
	"note" text,
	"actor_id" integer,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "request_items" (
	"id" serial PRIMARY KEY NOT NULL,
	"request_id" integer NOT NULL,
	"catalog_item_id" integer,
	"label" text NOT NULL,
	"quantity" integer DEFAULT 1 NOT NULL,
	"unit_price" integer DEFAULT 0 NOT NULL,
	"cost_estimate" integer DEFAULT 0 NOT NULL,
	"target" text
);
--> statement-breakpoint
CREATE TABLE "requests" (
	"id" serial PRIMARY KEY NOT NULL,
	"ref" text NOT NULL,
	"user_id" integer NOT NULL,
	"kind" text DEFAULT 'catalogue' NOT NULL,
	"title" text NOT NULL,
	"description" text,
	"category" text DEFAULT 'autre' NOT NULL,
	"target_name" text,
	"target_zone" text,
	"beneficiary_id" integer,
	"partner_id" integer,
	"status" text DEFAULT 'submitted' NOT NULL,
	"rejection_reason" text,
	"info_request" text,
	"amount_per_occurrence" integer DEFAULT 0 NOT NULL,
	"occurrences" integer DEFAULT 1 NOT NULL,
	"total_amount" integer DEFAULT 0 NOT NULL,
	"payment_mode" text DEFAULT 'immediate' NOT NULL,
	"recurrence" text DEFAULT 'none' NOT NULL,
	"start_date" timestamp with time zone,
	"annex_services" jsonb,
	"annex_cost" integer DEFAULT 0 NOT NULL,
	"execution_budget" integer DEFAULT 0 NOT NULL,
	"other_fees" integer DEFAULT 0 NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL,
	"completed_at" timestamp with time zone,
	"closed_at" timestamp with time zone,
	"archived_at" timestamp with time zone,
	CONSTRAINT "requests_ref_unique" UNIQUE("ref")
);
--> statement-breakpoint
CREATE TABLE "reviews" (
	"id" serial PRIMARY KEY NOT NULL,
	"kind" text NOT NULL,
	"ref_id" integer NOT NULL,
	"from_user_id" integer NOT NULL,
	"to_user_id" integer NOT NULL,
	"rating" integer NOT NULL,
	"comment" text,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "service_messages" (
	"id" serial PRIMARY KEY NOT NULL,
	"order_id" integer NOT NULL,
	"author_id" integer NOT NULL,
	"body" text NOT NULL,
	"price" integer,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "service_orders" (
	"id" serial PRIMARY KEY NOT NULL,
	"client_id" integer NOT NULL,
	"provider_id" integer NOT NULL,
	"offer_id" integer,
	"title" text NOT NULL,
	"description" text,
	"zone" text,
	"proposed_price" integer DEFAULT 0 NOT NULL,
	"agreed_price" integer,
	"compensation_mode" text DEFAULT 'money' NOT NULL,
	"exchange_description" text,
	"status" text DEFAULT 'requested' NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "sessions" (
	"id" text PRIMARY KEY NOT NULL,
	"user_id" integer NOT NULL,
	"expires_at" timestamp with time zone NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"user_agent" text
);
--> statement-breakpoint
CREATE TABLE "settings" (
	"key" text PRIMARY KEY NOT NULL,
	"value" jsonb NOT NULL,
	"description" text,
	"updated_by" integer,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "token_rules" (
	"id" serial PRIMARY KEY NOT NULL,
	"event_key" text NOT NULL,
	"label" text NOT NULL,
	"amount" integer DEFAULT 0 NOT NULL,
	"daily_cap" integer DEFAULT 0 NOT NULL,
	"active" boolean DEFAULT true NOT NULL,
	CONSTRAINT "token_rules_event_key_unique" UNIQUE("event_key")
);
--> statement-breakpoint
CREATE TABLE "token_transactions" (
	"id" serial PRIMARY KEY NOT NULL,
	"user_id" integer NOT NULL,
	"amount" integer NOT NULL,
	"type" text NOT NULL,
	"event_key" text,
	"reason" text,
	"ref_type" text,
	"ref_id" integer,
	"ref_key" text,
	"status" text DEFAULT 'approved' NOT NULL,
	"created_by" integer,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	CONSTRAINT "token_transactions_ref_key_unique" UNIQUE("ref_key")
);
--> statement-breakpoint
CREATE TABLE "user_badges" (
	"id" serial PRIMARY KEY NOT NULL,
	"user_id" integer NOT NULL,
	"code" text NOT NULL,
	"awarded_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "users" (
	"id" serial PRIMARY KEY NOT NULL,
	"phone" text NOT NULL,
	"name" text DEFAULT '' NOT NULL,
	"email" text,
	"role" text DEFAULT 'donor' NOT NULL,
	"permissions" jsonb DEFAULT '[]'::jsonb NOT NULL,
	"zone" text,
	"country" text DEFAULT 'Togo',
	"organization" text,
	"bio" text,
	"status" text DEFAULT 'active' NOT NULL,
	"notif_prefs" jsonb DEFAULT '{"inapp":true,"sms":true,"email":false}'::jsonb NOT NULL,
	"token_balance" integer DEFAULT 0 NOT NULL,
	"tokens_earned" integer DEFAULT 0 NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"last_login_at" timestamp with time zone,
	CONSTRAINT "users_phone_unique" UNIQUE("phone")
);
--> statement-breakpoint
CREATE INDEX "audit_created_idx" ON "audit_logs" USING btree ("created_at");--> statement-breakpoint
CREATE INDEX "mission_provider_idx" ON "missions" USING btree ("provider_id");--> statement-breakpoint
CREATE INDEX "notif_user_idx" ON "notifications" USING btree ("user_id");--> statement-breakpoint
CREATE INDEX "otp_phone_idx" ON "otp_codes" USING btree ("phone");--> statement-breakpoint
CREATE INDEX "pay_req_idx" ON "payments" USING btree ("request_id");--> statement-breakpoint
CREATE INDEX "req_user_idx" ON "requests" USING btree ("user_id");--> statement-breakpoint
CREATE INDEX "req_status_idx" ON "requests" USING btree ("status");--> statement-breakpoint
CREATE UNIQUE INDEX "review_unique" ON "reviews" USING btree ("kind","ref_id","from_user_id");--> statement-breakpoint
CREATE UNIQUE INDEX "badge_unique" ON "user_badges" USING btree ("user_id","code");