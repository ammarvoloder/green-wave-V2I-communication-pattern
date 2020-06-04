import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpResponse} from '@angular/common/http';
import { TrafficLight } from '../models/traffic-light';
import { Observable } from 'rxjs';






const endpoint = 'http://localhost:10113';


@Injectable({
    providedIn: 'root'
})
export class RestService {

    constructor(private http: HttpClient) {}

    getAllTrafficLights() : Observable<TrafficLight[]>{
        return this.http.get<TrafficLight[]>(endpoint + '/getAllTrafficLights');
    }
}