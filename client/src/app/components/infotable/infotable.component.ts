import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Movement } from '../../models/movement';
import { TrafficLight } from '../../models/traffic-light';
import { MatPaginator } from '@angular/material/paginator';

const MOVEMENT_DATA: Movement[] = [
  {vid: 'Auto1', speed: 60, longitude: 48.0079, latitude: 12.5542, crash: false, dateTime: null},
  {vid: 'Auto2', speed: 100, longitude: 48.0589, latitude: 12.1525, crash: true,  dateTime: null},
  {vid: 'Auto1', speed: 60, longitude: 48.0079, latitude: 12.5542, crash: false, dateTime: null},
  {vid: 'Auto2', speed: 100, longitude: 48.0589, latitude: 12.1525, crash: true,  dateTime: null},
  {vid: 'Auto1', speed: 60, longitude: 48.0079, latitude: 12.5542, crash: false, dateTime: null},
  {vid: 'Auto2', speed: 100, longitude: 48.0589, latitude: 12.1525, crash: true,  dateTime: null},
  {vid: 'Auto1', speed: 60, longitude: 48.0079, latitude: 12.5542, crash: false, dateTime: null},
  {vid: 'Auto2', speed: 100, longitude: 48.0589, latitude: 12.1525, crash: true,  dateTime: null},
  {vid: 'Auto1', speed: 60, longitude: 48.0079, latitude: 12.5542, crash: false, dateTime: null},
  {vid: 'Auto2', speed: 100, longitude: 48.0589, latitude: 12.1525, crash: true,  dateTime: null},
]

const TRAFFIC_LIGHT_DATA: TrafficLight[] = [
  {id: 1, longitude: 48.5528, latitude: 12.5142, statusGreen: false, statusChange: null, fullInfo: null},
  {id: 2, longitude: 48.0589, latitude: 12.1865, statusGreen: true,  statusChange: null, fullInfo: null},
]

@Component({
  selector: 'app-infotable',
  templateUrl: './infotable.component.html',
  styleUrls: ['./infotable.component.css']
})
export class InfotableComponent implements OnInit {

  @ViewChild('paginatorOne', {static: true}) movementPaginator: MatPaginator;
  @ViewChild('paginatorTwo', {static: true}) trafficLightPaginator: MatPaginator;

  movementDisplayedColumns: string[] = ['vid', 'speed', 'longitude', 'latitude', 'crash'];
  movementDataSource = new MatTableDataSource<Movement>(MOVEMENT_DATA);
  trafficLightDisplayedColumns: string[] = ['id', 'longitude', 'latitude', 'status'];
  trafficLightDataSource = new MatTableDataSource<TrafficLight>(TRAFFIC_LIGHT_DATA);
  

  constructor() { 
  }

  ngOnInit(): void {
    this.trafficLightDataSource.paginator = this.trafficLightPaginator;
    this.movementDataSource.paginator = this.movementPaginator;
  }

}
