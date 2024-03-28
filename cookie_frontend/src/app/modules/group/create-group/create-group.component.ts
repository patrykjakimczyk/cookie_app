import { Component } from '@angular/core';

import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { GroupService } from '../group.service';
import { GroupNameTakenResponse } from 'src/app/shared/model/responses/group-response';

@Component({
  selector: 'app-create-group',
  templateUrl: './create-group.component.html',
  styleUrls: ['./create-group.component.scss'],
})
export class CreateGroupComponent {
  protected createGroupSucceded = false;
  protected groupNameRegex = RegexConstants.groupNameRegex;
  protected groupNameTaken = false;

  constructor(private groupService: GroupService) {}

  createGroup(name: string) {
    this.groupService
      .createGroup({ groupName: name })
      .subscribe((response: GroupNameTakenResponse) => {
        if (response.groupNameTaken) {
          this.groupNameTaken = true;
        } else {
          this.groupNameTaken = false;
          this.createGroupSucceded = true;
        }
      });
  }
}
