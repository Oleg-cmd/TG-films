import axios, {
  AxiosInstance,
  InternalAxiosRequestConfig,
  AxiosResponse,
  AxiosError,
} from "axios";

// const API_URL = "https://localhost:8080"; // Замените на ваш базовый URL
const API_URL =
  "https://jewellery-worship-minor-unauthorized.trycloudflare.com";

const axiosInstance: AxiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // console.log(`Request sent to: ${config.url}`);
    return config;
  },
  (error: AxiosError) => {
    console.error("Request error:", error);
    return Promise.reject(error);
  }
);

// Response interceptor
axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => {
    // console.log('Response received:', response);
    return response;
  },
  (error: AxiosError) => {
    console.error("Response error:", error);
    return Promise.reject(error);
  }
);

export default axiosInstance;
