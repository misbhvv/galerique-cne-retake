const API_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

const login = (identifier: string, password: string, remember: boolean = false) => {
	return fetch(`${API_URL}/auth/login?remember=${remember}`, {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		credentials: "include",
		body: JSON.stringify({ identifier, password }),
	});
};

const logout = (hard: boolean = false) => {
	return fetch(`${API_URL}/auth/logout?hard=${hard}`, {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		credentials: "include",
	});
};

const status = async () => {
	return fetch(`${API_URL}/auth/status`, {
		method: "GET",
		credentials: "include",
		headers: { "Content-Type": "application/json" },
	});
};

const AuthService = {
	login,
	logout,
	status,
};

export default AuthService;
