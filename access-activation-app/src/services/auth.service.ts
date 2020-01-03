import {ajax} from './index';
import { IUser } from '../interfaces';

export const loginEmployee = (email: string, password: string) => {
    return ajax.post('/auth/login', {
        email,
        password
    })
}

export const registerEmployee = (employee: IUser) => {
    return ajax.post('/employee', employee)
}

export const verifyImage = (userId: string, file: any) => {
    let formData = new FormData();
    formData.append('userName', userId);
    formData.append('file', file);
    return ajax.post('/facerecognition/loginwithface', formData, {
        headers: {'Content-Type': 'multipart/form-data'}
    })
}

export const registerImage = (userId: string, file: any) => {
    let formData = new FormData();
    formData.append('userName', userId);
    formData.append('file', file);
    return ajax.post('/facerecognition/upload', formData, {
        headers: {'Content-Type': 'multipart/form-data'}
    })
}