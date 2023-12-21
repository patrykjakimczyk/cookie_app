import { authorityEnums } from './../../../shared/model/enums/authority-enum';
import { ActivatedRoute, Router } from '@angular/router';
import { Component, Input, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';

import { NewNamePopupComponentComponent } from 'src/app/shared/components/new-name-popup-component/new-name-popup-component.component';
import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { DeletePopupComponent } from 'src/app/shared/components/delete-popup/delete-popup.component';
import { GroupDetailsDTO } from 'src/app/shared/model/types/group-types';
import { GroupService } from './../group.service';
import { AuthorityDTO, UserDTO } from 'src/app/shared/model/types/user-types';
import { HttpErrorResponse } from '@angular/common/http';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { AuthorityEnum } from 'src/app/shared/model/enums/authority-enum';
import { FormBuilder, Validators } from '@angular/forms';
import { UserService } from 'src/app/shared/services/user-service';

@Component({
  selector: 'app-group-details',
  templateUrl: './group-details.component.html',
  styleUrls: ['./group-details.component.scss'],
})
export class GroupDetailsComponent implements OnInit {
  @Input({ required: true }) groupId!: number;
  private authoritiesToRemove: AuthorityEnum[] = [];
  protected group: GroupDetailsDTO | null = null;
  protected authorityEnum = AuthorityEnum;

  protected addAuthorityForm = this.fb.group({
    authority: ['', Validators.required],
  });

  constructor(
    private groupService: GroupService,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private fb: FormBuilder,
    protected userService: UserService
  ) {}

  ngOnInit(): void {
    this.groupId = this.route.snapshot.params['id'];

    this.getGroupDetails();
  }

  checkboxClicked(event: MatCheckboxChange, value: AuthorityDTO) {
    if (event.checked) {
      this.authoritiesToRemove.push(value.authority);
    } else {
      this.authoritiesToRemove.filter(
        (authority) => authority !== value.authority
      );
    }
  }

  possibleAuthoritiesToAdd(authorities: AuthorityDTO[]) {
    const userAuthorityEnums = authorities.map(
      (authority) => authority.authority
    );

    return authorityEnums.filter(
      (authority) => !userAuthorityEnums.includes(authority)
    );
  }

  submitAddAuthority(userId: number) {
    if (!this.addAuthorityForm.valid) {
      return;
    }

    this.groupService
      .assignAuthoritiesToUser(this.groupId, {
        userId: userId,
        authorities: [
          this.addAuthorityForm.value['authority'] as AuthorityEnum,
        ],
      })
      .subscribe({
        next: (response) => {
          if (this.group) {
            const user = this.group.users.find((user) => user.id === userId);

            if (user) {
              for (let authorityDTO of response.assignedAuthorities) {
                user.authorities.push(authorityDTO);
              }
            }
          }

          this.addAuthorityForm.reset();
          this.addAuthorityForm.controls.authority.setErrors(null);
        },
      });
  }

  removeAuthorities(userId: number) {
    if (this.group) {
      this.groupService
        .removeAuthoritiesFromUser(this.group?.id, {
          userId: userId,
          authorities: this.authoritiesToRemove,
        })
        .subscribe({
          next: (_) => {
            if (this.group) {
              const user = this.group.users.find((user) => user.id === userId);

              if (user) {
                for (let authority of this.authoritiesToRemove) {
                  user.authorities = user.authorities.filter(
                    (userAuthority) => userAuthority.authority !== authority
                  );
                }
              }
            }
          },
          error: (error: HttpErrorResponse) => {
            if (error.status === 403) {
              this.snackBar.open(
                `User doesn't exists or you tried to remove authorities from user without permissions`,
                'Okay'
              );
            }
          },
        });
    }
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
      data: {
        header: 'Are you sure you want to delete this group?',
        button: 'Delete group',
      },
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

  openAddUserPopup() {
    const changeGroupNamePopup = this.dialog.open(
      NewNamePopupComponentComponent,
      { data: { type: 'USER', regex: RegexConstants.loginRegex } }
    );

    changeGroupNamePopup.afterClosed().subscribe((username: string) => {
      this.groupService
        .addUserToGroup(this.groupId, { usernameToAdd: username })
        .subscribe({
          next: (_: any) => {
            this.getGroupDetails();
            this.snackBar.open(
              `User: ${username} has been added to the group`,
              'Okay'
            );
          },
          error: (error: HttpErrorResponse) => {
            if (error.status === 403) {
              this.snackBar.open(
                `User doesn't exists or you tried to add user without permissions`,
                'Okay'
              );
            } else if (error.status === 409) {
              this.snackBar.open(
                `User is already a member of the group`,
                'Okay'
              );
            }
          },
        });
    });
  }

  openRemoveUserPopup(user: UserDTO) {
    const removeUserDialog = this.dialog.open(DeletePopupComponent, {
      data: {
        header: 'Are you sure you want to remove this user from group?',
        button: 'Remove user',
      },
    });

    removeUserDialog.afterClosed().subscribe((removeUser) => {
      if (removeUser) {
        this.groupService.removeUserFromGroup(this.groupId, user.id).subscribe({
          next: (_) => {
            this.snackBar.open(
              `Group: ${user.username} has been removed from group`,
              'Okay'
            );

            this.getGroupDetails();
          },
          error: (error: HttpErrorResponse) => {
            if (error.status === 403) {
              this.snackBar.open(
                `User doesn't exists or you tried to assign authorities which are already assigned to user, or you don't have permissions to do that`,
                'Okay'
              );
            }
          },
        });
      }
    });
  }

  private getGroupDetails() {
    this.groupService.getGroup(this.groupId).subscribe({
      next: (response) => {
        this.group = response;

        const user = this.userService.user.getValue();
        response.users.forEach((userDTO) => {
          if (userDTO.username === user.username) {
            this.userService.setUserAuthorities(userDTO.authorities);
          }
        });
      },
      error: (error) => {
        this.router.navigate(['/']);
      },
    });
  }
}
