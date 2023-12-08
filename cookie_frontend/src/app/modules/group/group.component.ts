import { GroupDTO } from 'src/app/shared/model/types/group-types';
import { GroupService } from './group.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-group',
  templateUrl: './group.component.html',
  styleUrls: ['./group.component.scss'],
})
export class GroupComponent implements OnInit {
  protected userGroups: GroupDTO[] = [];

  constructor(private groupService: GroupService) {}

  ngOnInit(): void {
    this.groupService.getUserGroups().subscribe({
      next: (response) => {
        this.userGroups = response.userGroups;
      },
    });
  }
}
