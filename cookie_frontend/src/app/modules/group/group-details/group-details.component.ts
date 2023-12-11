import { ActivatedRoute, Router } from '@angular/router';
import { GroupService } from './../group.service';
import { Component, Input, OnInit } from '@angular/core';
import { GroupDetailsDTO } from 'src/app/shared/model/types/group-types';
import { MatDialog } from '@angular/material/dialog';
import { NewNamePopupComponentComponent } from 'src/app/shared/components/new-name-popup-component/new-name-popup-component.component';
import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { DeletePopupComponent } from 'src/app/shared/components/delete-popup/delete-popup.component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-group-details',
  templateUrl: './group-details.component.html',
  styleUrls: ['./group-details.component.scss'],
})
export class GroupDetailsComponent implements OnInit {
  @Input({ required: true }) groupId!: number;
  protected group: GroupDetailsDTO | null = null;

  constructor(
    private groupService: GroupService,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.groupId = this.route.snapshot.params['id'];

    this.groupService.getGroup(this.groupId).subscribe({
      next: (response) => {
        this.group = response;
        console.log(this.group);
      },
      error: (_) => {
        this.router.navigate(['/']);
      },
    });
  }

  openChangeGroupNamePopup() {
    const changeGroupNamePopup = this.dialog.open(
      NewNamePopupComponentComponent,
      { data: { type: 'GROUP', regex: RegexConstants.groupNameRegex } }
    );

    changeGroupNamePopup.afterClosed().subscribe((newGroupName: string) => {
      this.groupService
        .updateGroup(this.groupId, { newGroupName: newGroupName })
        .subscribe({
          next: (_: any) => {
            if (this.group) {
              this.group.groupName = newGroupName;
            }
          },
        });
    });
  }

  openDeleteGroupPopup() {
    const deleteGroupDialog = this.dialog.open(DeletePopupComponent, {
      data: 'GROUP',
    });

    deleteGroupDialog.afterClosed().subscribe((deleteGroup) => {
      if (deleteGroup) {
        this.groupService.deleteGroup(this.groupId).subscribe({
          next: (_) => {
            this.snackBar.open(
              `Group: ${this.group?.groupName} has been deleted`,
              'Okay'
            );
            this.router.navigate(['/groups']);
          },
        });
      }
    });
  }
}
