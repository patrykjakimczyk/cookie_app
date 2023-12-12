import { Component, OnInit } from '@angular/core';

import { GroupDTO } from 'src/app/shared/model/types/group-types';
import { GroupService } from './group.service';

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
        console.log(response.userGroups);
        this.userGroups = response.userGroups;
      },
    });
  }
}
