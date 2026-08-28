import Link from "next/link";

export default function PrivacyPolicy() {
	return (
		<div className="min-h-screen bg-white dark:bg-zinc-950">
			{/* Hero */}
			<section className="relative overflow-hidden">
				<div className="absolute inset-0 pointer-events-none">
					<div className="absolute top-0 left-1/2 -translate-x-1/2 w-[600px] h-[600px] bg-gradient-to-b from-zinc-100 dark:from-zinc-900/50 to-transparent rounded-full blur-3xl opacity-50" />
				</div>
				<div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 pt-32 pb-16">
					<p className="text-sm uppercase tracking-[0.3em] text-zinc-400 dark:text-zinc-600 mb-4 font-medium animate-fade-in">
						Legal
					</p>
					<h1 className="font-[var(--font-bricolage)] text-4xl sm:text-5xl font-bold text-zinc-900 dark:text-white animate-fade-in stagger-1">
						Privacy Policy
					</h1>
					<p className="mt-4 text-zinc-500 dark:text-zinc-400 text-lg animate-fade-in stagger-2">
						Last updated: January 2025
					</p>
				</div>
			</section>

			{/* Content */}
			<section className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 pb-24">
				<div className="prose prose-zinc dark:prose-invert max-w-none prose-headings:font-[var(--font-bricolage)] prose-h2:text-2xl prose-h2:mt-12 prose-h2:mb-4 prose-p:text-zinc-600 dark:prose-p:text-zinc-400 prose-p:leading-relaxed prose-li:text-zinc-600 dark:prose-li:text-zinc-400">
					<div className="h-px bg-zinc-200 dark:bg-zinc-800 mb-12" />

					<h2>1. Introduction</h2>
					<p>
						Welcome to Galerique (&quot;we,&quot; &quot;our,&quot; or &quot;us&quot;). We are committed to
						protecting your privacy and ensuring the security of your personal information. This Privacy
						Policy explains how we collect, use, disclose, and safeguard your information when you visit our
						platform and use our services.
					</p>

					<h2>2. Information We Collect</h2>
					<p>We may collect information about you in a variety of ways, including:</p>
					<ul>
						<li>
							<strong>Account Information:</strong> When you create an account, we collect your username,
							email address, and encrypted password.
						</li>
						<li>
							<strong>Profile Information:</strong> Any additional information you choose to provide, such
							as a biography, profile picture, or social media links.
						</li>
						<li>
							<strong>Content:</strong> Artworks, descriptions, and other content you upload or publish on
							Galerique.
						</li>
						<li>
							<strong>Usage Data:</strong> Information about how you interact with our platform, including
							pages visited, features used, and time spent.
						</li>
						<li>
							<strong>Device Information:</strong> Browser type, operating system, IP address, and other
							technical data collected automatically.
						</li>
					</ul>

					<h2>3. How We Use Your Information</h2>
					<p>We use the information we collect to:</p>
					<ul>
						<li>Provide, maintain, and improve our platform and services</li>
						<li>Process transactions and manage your account</li>
						<li>Personalize your experience and deliver relevant content</li>
						<li>Communicate with you about updates, promotions, and support</li>
						<li>Monitor and analyze usage patterns and trends</li>
						<li>Protect against unauthorized access and ensure platform security</li>
					</ul>

					<h2>4. Information Sharing</h2>
					<p>
						We do not sell your personal information. We may share your data only in the following
						circumstances:
					</p>
					<ul>
						<li>
							<strong>Public Profile:</strong> Your username, profile picture, and published artworks are
							visible to other users.
						</li>
						<li>
							<strong>Service Providers:</strong> We may share data with trusted third-party services that
							help us operate our platform.
						</li>
						<li>
							<strong>Legal Requirements:</strong> We may disclose information if required by law or in
							response to valid legal processes.
						</li>
					</ul>

					<h2>5. Data Security</h2>
					<p>
						We implement industry-standard security measures to protect your personal information, including
						encryption of passwords and secure data transmission. However, no method of transmission over
						the internet is 100% secure.
					</p>

					<h2>6. Cookies</h2>
					<p>
						We use cookies and similar technologies to authenticate users, remember preferences, and analyze
						platform usage. You can control cookie settings through your browser preferences.
					</p>

					<h2>7. Your Rights</h2>
					<p>You have the right to:</p>
					<ul>
						<li>Access and receive a copy of your personal data</li>
						<li>Request correction or deletion of your personal data</li>
						<li>Withdraw consent for data processing at any time</li>
						<li>Object to processing of your personal data</li>
						<li>Request data portability</li>
					</ul>

					<h2>8. Data Retention</h2>
					<p>
						We retain your personal information for as long as your account is active or as needed to
						provide our services. You may request deletion of your account and associated data at any time.
					</p>

					<h2>9. Changes to This Policy</h2>
					<p>
						We may update this Privacy Policy from time to time. We will notify you of any changes by
						posting the new policy on this page and updating the &quot;Last updated&quot; date.
					</p>

					<h2>10. Contact Us</h2>
					<p>
						If you have any questions about this Privacy Policy, please contact us through our{" "}
						<Link
							href="/about"
							className="text-zinc-900 dark:text-white underline underline-offset-4"
						>
							About page
						</Link>
						.
					</p>
				</div>
			</section>
		</div>
	);
}
